package com.github.fabricservertools.deltalogger.dao;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.fabricservertools.deltalogger.SQLUtils;

import org.jdbi.v3.core.Jdbi;

public class AuthDAO {
  private Jdbi jdbi;
  private SecureRandom sr;
  private String jwtSecret;
  private JWTVerifier jwtVerifier;

  public AuthDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
    sr = new SecureRandom();

    jwtSecret = jdbi.withHandle(handle -> handle
      .createQuery("SELECT value FROM kv_store WHERE key='jwt_secret'")
      .mapTo(String.class)
      .findOne()
    ).orElseGet(() -> {
      String generated = b64Encode(genSalt());
      int result = jdbi.withHandle(handle -> handle
          .createUpdate("INSERT INTO kv_store (key, value) VALUES ('jwt_secret',?)")
          .bind(0, generated)
          .execute());
      if (result != 1) {
        throw new Error("Problem generating jwt secret. Aborting.");
      }
      return generated;
    });

    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
    jwtVerifier = JWT.require(algorithm).build();

    // JWTGenerator<MockUser> generator = (user, alg) -> {
    //   JWTCreator.Builder token = JWT.create()
    //           .withClaim("name", user.name)
    //           .withClaim("level", user.level);
    //   return token.sign(alg);
    // };
  }

  private byte[] genSalt() {
    byte[] salt = new byte[16];
    sr.nextBytes(salt);
    return salt;
  }

  private String b64Encode(byte[] bytes) {
    return new String(Base64.getEncoder().encode(bytes));
  }

  private byte[] b64Decodde(String b64) {
    return Base64.getDecoder().decode(b64);
  }

  private byte[] hashPass(String pass, byte[] salt) {
    KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
    SecretKeyFactory factory = null;
    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return factory.generateSecret(spec).getEncoded();

    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Optional<DecodedJWT> verifyJWT(String token) {
    DecodedJWT djwt = null;
    try {
      djwt = jwtVerifier.verify(token);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.ofNullable(djwt);
  }

  public String issueTemporaryPass(UUID user, boolean isLevel4Op) {
    byte[] salt = genSalt();
    String tempPass = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    String hashedPass;
    try {
      hashedPass = b64Encode(hashPass(tempPass, salt));
      jdbi.withHandle(handle -> {
        return handle
            .createUpdate(String.join(" ",
                "INSERT INTO perms",
                "(player_id, password_hash, temporary_pass, salt, roll_back, `delete`)",
                "SELECT players.id, :hash, 1, :salt, :roll_back, :delete",
                "FROM players WHERE uuid=:uuid",
                SQLUtils.onDuplicateKeyUpdate("player_id"), "temporary_pass=1, password_hash=:hash, salt=:salt"))
            .bind("uuid", user.toString())
            .bind("hash", hashedPass)
            .bind("salt", b64Encode(salt))
            .bind("roll_back", isLevel4Op)
            .bind("delete", isLevel4Op)
            .execute();
      });
    } catch (Exception e) {
      e.printStackTrace();
    }

    return tempPass;
  }

  public Optional<Map<String, Object>> getUserFromUserName(String username) {
    return jdbi.withHandle(handle -> handle
      .select(
        String.join(" ",
          "SELECT players.uuid as uuid, password_hash, salt, temporary_pass",
          "FROM perms INNER JOIN players on perms.player_id = players.id",
          "WHERE UPPER(players.name) = UPPER(?)"
        ),
        username
      )
      .mapToMap()
      .findOne()
    );
  }

  public Optional<String> generateJWT(String username, String password) {
    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

    return getUserFromUserName(username)
      .map(m -> {
        String userHash = b64Encode(hashPass(password, b64Decodde((String) m.get("salt"))));
        
        if (userHash != null && userHash.equals((String)m.get("password_hash"))) {
          return JWT.create()
            .withClaim("user_id", (String) m.get("uuid"))
            .withClaim("user_name", username.toLowerCase())
            .withClaim("temporary", (Integer) m.get("temporary_pass") == 1)
            .withIssuedAt(new Date())
            .sign(algorithm);
        }
        return null;
      });
  }

  public Optional<String> changePass(String user, String newPassword, boolean temporary) {
    byte[] salt = genSalt();
    String b64salt = b64Encode(salt);
    String b64pass = b64Encode(hashPass(newPassword, salt));
    boolean success = jdbi.withHandle(handle -> handle
      .createUpdate(String.join(" ",
        "UPDATE perms",
        "SET password_hash=:hash, salt=:salt, temporary_pass=:temp",
        "WHERE player_id=(SELECT id FROM players WHERE UPPER(name)=UPPER(:name))"
      ))
      .bind("name", user)
      .bind("hash", b64pass)
      .bind("salt", b64salt)
      .bind("temp", temporary)
      .execute()
    ) == 1;
    if (success) {
      return generateJWT(user, newPassword);
    }
    return Optional.empty();
  }

}
