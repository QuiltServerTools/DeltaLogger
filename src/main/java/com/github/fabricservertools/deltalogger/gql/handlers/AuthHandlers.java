package com.github.fabricservertools.deltalogger.gql.handlers;

import java.util.HashMap;
import java.util.Optional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.fabricservertools.deltalogger.dao.DAO;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

public class AuthHandlers {
  public static final Handler validateHandler = ctx -> {
    if (ctx.method() == "OPTIONS") return;
    Optional<DecodedJWT> ojwt = Optional
      .ofNullable(ctx.header("Authorization"))
      .map(hv -> hv.replaceFirst("^Bearer ", ""))
      .map(token -> token.isEmpty() ? null : token)
      .flatMap(token -> {
        return DAO.auth.verifyJWT(token);
      });

    if (ojwt.isPresent()) {
      ctx.attribute("token", ojwt.get());
    } else {
      throw new UnauthorizedResponse();
    }
  };

  private static class LoginRequestBody {
    public String username;
    public String password;
  }
  public static final Handler provideJwtHandler = ctx -> {
    LoginRequestBody authInfo = ctx.bodyValidator(LoginRequestBody.class).get();
    Optional<String> oToken = DAO.auth.generateJWT(authInfo.username, authInfo.password);
    if (oToken.isPresent()) {
      HashMap<String, String> resp = new HashMap<>();
      resp.put("token", oToken.get());
      ctx.json(resp);
    } else {
      throw new UnauthorizedResponse();
    }
  };

  private static class ChangePassBody {
    public String password;
  }
  public static final Handler changePassHandler = ctx -> {
    ChangePassBody body = ctx.bodyValidator(ChangePassBody.class).get();
    DecodedJWT jwt = (DecodedJWT) ctx.attribute("token");
    Optional<String> oToken = DAO.auth.changePass(
      jwt.getClaim("user_name").asString(),
      body.password,
      false
    );
    if (oToken.isPresent()) {
      HashMap<String, String> resp = new HashMap<>();
      resp.put("token", oToken.get());
      ctx.status(200).json(resp);
    } else {
      throw new BadRequestResponse();
    }
  };
}
