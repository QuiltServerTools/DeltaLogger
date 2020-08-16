package dev.lambdacraft.watchtower;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import com.google.common.collect.Queues;
import com.google.common.io.Resources;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import dev.lambdacraft.watchtower.beans.Placement;
import dev.lambdacraft.watchtower.beans.Player;
import dev.lambdacraft.watchtower.beans.Transaction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Handles inserting POJOs into the database. All POJOs are inserted into a
 * queue and pulled in order of priority. This is to ensure that foreign table
 * references are processed in the correct order, otherwise foreign key
 * constraints would fail. Foreign key constraints have been removed from the
 * SQL schema because shared host providers use outdated MySQL versions that
 * lack foreign key constraints.
 */
public class DatabaseManager implements Runnable {
  private static DatabaseManager manager;
  public static DateTimeFormatter timeFormat;
  static {
    timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
  }

  private Jdbi jdbi;
  private PriorityBlockingQueue<QueueOperation> pq = new PriorityBlockingQueue<>(10, Comparator.comparingInt(QueueOperation::getPriority));
  private final AtomicBoolean running = new AtomicBoolean(false);
  public static final Logger LOG = LogManager.getLogger();

  private DatabaseManager(Properties config) {
    try {
      initJdbi(getDataSource(config));
      LOG.info("WatchTower started");
    } catch (IOException e) {
      e.printStackTrace();
      // System.exit(0);
    }
  }

  private Jdbi initJdbi(DataSource source) throws IOException {
    jdbi = Jdbi.create(source);
    createTables();
    jdbi.registerRowMapper(Placement.class, (rs, ctx) -> {
      return new Placement(
        rs.getString("playername"),
        rs.getTimestamp("date").toInstant(),
        new Identifier(rs.getString("blockname")),
        new BlockPos(rs.getInt("x"), rs.getInt("y"), rs.getInt("z")),
        rs.getBoolean("placed"),
        new Identifier(rs.getString("dimension"))
      );
    });
    jdbi.registerRowMapper(Transaction.class, (rs, ctx) -> new Transaction(
      rs.getTimestamp("date").toInstant(),
      rs.getString("playername"),
      new Identifier(rs.getString("itemname")),
      rs.getInt("itemcount"),
      UUID.fromString(rs.getString("uuid"))
    ));
    return jdbi;
  }

  public static DatabaseManager init(Properties config) {
    manager = new DatabaseManager(config);
    return manager;
  }

  public static DatabaseManager getSingleton() {
    return manager;
  }

  /**
   * Configure and init Hikari connection pool
   */
  private static HikariDataSource getDataSource(Properties props) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.join("",
      "jdbc:mysql://", props.getProperty("host"), ":", props.getProperty("port"), "/", props.getProperty("database")
    ));
    config.setUsername(props.getProperty("username"));
    config.setPassword(props.getProperty("password"));

    config.addDataSourceProperty("useLegacyDate‌timeCode", "false");
    config.addDataSourceProperty("serverTimezone", "UTC");

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");

    int maxLifetime;
    try {
      maxLifetime = Integer.parseInt(props.getProperty("maxLifetime", "290000"));
    } catch (Exception e) {
      maxLifetime = 290000;
      LOG.warn("Invalid maxLifetime value. Using default " + maxLifetime);
    }

    config.setMaxLifetime(maxLifetime);

    config.addDataSourceProperty("useSSL", props.getProperty("useSSL", "true"));
    config.addDataSourceProperty("requireSSL", props.getProperty("requireSSL", "false"));
    config.addDataSourceProperty("verifyServerCertificate", "false");

    return new HikariDataSource(config);
  }

  public void createTables() throws IOException {
    URL url = DatabaseManager.class.getResource("/data/watchtower/tables.sql");
    String tableSql = Resources.toString(url,  StandardCharsets.UTF_8);
    jdbi.useHandle(handle -> {
      handle.createScript(tableSql).execute();
      // handle.execute("CREATE FUNCTION BIN_TO_UUID(b BINARY(16), f BOOLEAN) RETURNS CHAR(36) DETERMINISTIC BEGIN DECLARE hexStr CHAR(32); SET hexStr = HEX(b); RETURN LOWER(CONCAT(IF(f,SUBSTR(hexStr, 9, 8),SUBSTR(hexStr, 1, 8)), '-', IF(f,SUBSTR(hexStr, 5, 4),SUBSTR(hexStr, 9, 4)), '-', IF(f,SUBSTR(hexStr, 1, 4),SUBSTR(hexStr, 13, 4)), '-', SUBSTR(hexStr, 17, 4), '-', SUBSTR(hexStr, 21))); END;");
      // handle.execute("CREATE FUNCTION UUID_TO_BIN(uuid CHAR(36), f BOOLEAN) RETURNS BINARY(16) DETERMINISTIC BEGIN RETURN UNHEX(CONCAT(IF(f,SUBSTRING(uuid, 15, 4),SUBSTRING(uuid, 1, 8)),SUBSTRING(uuid, 10, 4),IF(f,SUBSTRING(uuid, 1, 8),SUBSTRING(uuid, 15, 4)),SUBSTRING(uuid, 20, 4),SUBSTRING(uuid, 25))); END;");
    });
  }

  public static String getTime() {
    return timeFormat.format(java.time.Instant.now());
  }

  /**
   * QUEUE Operation classes to insert into the priority queue
   */
  static abstract class QueueOperation {
    public abstract PreparedBatch addBindings(PreparedBatch batch);
    public abstract PreparedBatch prepareBatch(Handle handle);
    public abstract int getPriority();

    protected PreparedBatch batch;

    public int[] execute(Handle handle) {
      if (this.batch == null) this.batch = this.prepareBatch(handle);
      return addBindings(batch).execute();
    }
  }

  static class PlayerUpdate extends QueueOperation {
    private Player p;
    public int getPriority() { return 1; }

    PlayerUpdate(Player p) { this.p = p; }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join(" ",
        "INSERT INTO players (uuid, name, lastonline) VALUES (:id, :name, :lastonline)",
        "ON DUPLICATE KEY UPDATE uuid=:id, name=:name, lastonline=:lastonline"
      ));
    }

    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("id", p.getUuid().toString())
        .bind("name", p.getName())
        .bind("lastonline", p.getLastOnline())
        .add();
    }
  }

  static class RegistryUpdate extends QueueOperation {
    private Identifier id;
    public int getPriority() { return 0; }

    RegistryUpdate(Identifier id) { this.id = id; }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch("INSERT INTO registry (name) SELECT :name WHERE NOT EXISTS (SELECT 1 FROM registry WHERE name=:name)");
    }

    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch.bind("name", this.id.toString()).add();
    }
  }

  public static class BlockUpdate extends QueueOperation {
    private String date;
    private boolean placed;
    private BlockPos pos;
    private UUID playerid;
    private Identifier blockid;
    private Identifier dimensionid;
    public int getPriority() { return 2; }

    public BlockUpdate(UUID playerid, Identifier blockid, boolean placed, BlockPos pos, Identifier dimensionid) {
      this.date = DatabaseManager.getTime();
      this.placed = placed;
      this.pos = pos;
      this.playerid = playerid;
      this.blockid = blockid;
      this.dimensionid = dimensionid;
    }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join(" ",
        "INSERT INTO placements (date, placed, x, y, z, playerid, type, dimensionid)",
        "SELECT :date, :placed, :x, :y, :z,",
          "(SELECT id FROM players WHERE uuid=:playeruuid),",
          "(SELECT id FROM registry WHERE name=:blockid),",
          "(SELECT id FROM registry WHERE name=:dimensionid)"
      ));
    }

    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("date", date)
        .bind("placed", placed)
        .bind("x", pos.getX()).bind("y", pos.getY()).bind("z", pos.getZ())
        .bind("playeruuid", playerid.toString())
        .bind("blockid", blockid.toString())
        .bind("dimensionid", dimensionid.toString())
        .add();
    }
  }

  public static class ContainerUpdate extends QueueOperation {
    private UUID uuid;
    private String lastaccess;
    private BlockPos pos;
    private UUID accessingPlayer;
    private Identifier blockId;
    private Identifier dimension;
    public int getPriority() { return 2; }

    public ContainerUpdate(UUID uuid, Identifier blockId, BlockPos pos, UUID accessingPlayer, String lastaccess, Identifier dimension) {
      this.uuid = uuid;
      this.lastaccess = lastaccess;
      this.pos = pos;
      this.accessingPlayer = accessingPlayer;
      this.blockId = blockId;
      this.dimension = dimension;
    }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join(" ",
        "INSERT INTO containers (uuid, lastaccess, x, y, z, firstplayer, lastplayer, itemtype)",
        "SELECT :uuid, :lastaccess, :x, :y, :z, players.id, players.id, registry.id",
        // "(SELECT id FROM registry WHERE name=:dimensionid)",
        "FROM players, registry",
        "WHERE players.uuid=:player AND registry.name=:itemtype",
        "ON DUPLICATE KEY UPDATE lastaccess=:lastaccess, x=:x, y=:y, z=:z, lastplayer=players.id"
      ));
    }

    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("uuid", uuid.toString())
        .bind("lastaccess", lastaccess)
        .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
        .bind("player", accessingPlayer.toString())
        .bind("itemtype", blockId.toString())
        .add();
        // .bind("dimensionid", dimension.toString())
    }
  }

  public static class ContainerTransaction extends QueueOperation {
    private UUID playerId;
    private UUID containerId;
    private String date;
    private Identifier itemtype;
    private Integer itemcount;
    private byte[] itemdata;

    public int getPriority() { return 3; }

    public ContainerTransaction(UUID playerId, UUID containerId, String date, Identifier itemtype, Integer itemcount, byte[] itemdata) {
      this.playerId = playerId;
      this.containerId = containerId;
      this.date = date;
      this.itemtype = itemtype;
      this.itemcount = itemcount;
      this.itemdata = itemdata;
    }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join("",
        "INSERT INTO container_transactions (playerid, containerid, date, itemtype, itemcount, itemdata)",
        "SELECT players.id, containers.id, :date, registry.id, :itemcount, :itemdata ",
        "FROM players, containers, registry ",
        "WHERE players.uuid=:playerId AND containers.uuid=:containerId AND registry.name=:itemtype "
      ));
    }

    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("playerId", playerId.toString())
        .bind("containerId", containerId.toString())
        .bind("date", date)
        .bind("itemtype", itemtype.toString())
        .bind("itemcount", itemcount)
        .bind("itemdata", itemdata)
        .add();
    }
  }

  public static class EntityKillUpdate extends QueueOperation {
    private String name;
    private String sourceName;
    private UUID killerid;
    private String date;
    private BlockPos pos;
    public int getPriority() { return 2; }

    public EntityKillUpdate(String name, String sourceName, UUID killerid, String date, BlockPos pos) {
      this.name = name;
      this.sourceName = sourceName;
      this.killerid = killerid;
      this.date = date;
      this.pos = pos;
    }
    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join("",
        "INSERT INTO killed_entities (name, source, killerid, date, x, y, z) ",
        "VALUES (:name, :source, ",
          "IF(:killerid IS NULL, NULL, (SELECT id FROM players WHERE uuid=:killerid)), ",
          ":date, :x, :y, :z)"
      ));
    }
    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("name", name)
        .bind("source", sourceName)
        .bind("killerid", killerid != null ? killerid.toString() : null)
        .bind("date", date)
        .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
        .add();
    }
  }

  public static class MobGriefUpdate extends QueueOperation {
    private UUID target;
    private String date;
    private Identifier entityType;
    private BlockPos pos;

    public MobGriefUpdate(UUID target, String date, Identifier entityType, BlockPos pos) {
      this.target = target;
      this.date = date;
      this.entityType = entityType;
      this.pos = pos;
    }

    public PreparedBatch prepareBatch(Handle handle) {
      return handle.prepareBatch(String.join("",
        "INSERT INTO mob_grief (date, entity_type, target, x, y, z) ",
        "SELECT :date, registry.id, players.id, :x, :y, :z ",
        "FROM players, registry ",
        "WHERE players.uuid=:target AND registry.name=:entityType "
      ));
    }
    public PreparedBatch addBindings(PreparedBatch batch) {
      return batch
        .bind("date", date)
        .bind("target", target.toString())
        .bind("entityType", entityType.toString())
        .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
        .add();
    }

    public int getPriority() {
      return 2;
    }
  }

  public void queuePlayerUpdate(Player p) {
    pq.add(new PlayerUpdate(p));
  }

  public void queueRegistryUpdate(Identifier id) {
    pq.add(new RegistryUpdate(id));
  }

  public void queueOp(QueueOperation op) { pq.add(op); }

  public List<Placement> getPlacementsAt(Identifier dimension, BlockPos pos, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
        .select(
          String.join(" ",
            "SELECT P.name AS `playername`, date, IT.name AS `blockname`, x, y, z, placed, DT.name as `dimension`",
            "FROM placements",
            "INNER JOIN players as P ON P.id=playerid",
            "INNER JOIN registry as IT ON IT.id=type",
            "INNER JOIN registry as DT ON DT.id=dimensionid",
            "WHERE x = ? AND y = ? AND z = ? AND DT.name = ?",
            "ORDER BY date DESC LIMIT ?"
          ),
          pos.getX(), pos.getY(), pos.getZ(),
          dimension.toString(), limit
        )
        .mapTo(Placement.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Transaction> getTransactionsAt(Identifier dimension, BlockPos pos, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
        .select(
          String.join(" ",
            "SELECT C.uuid, CT.date, R.name as `itemname`, CT.itemcount, P.name as `playername`",
            "FROM container_transactions as CT",
            "INNER JOIN registry as R ON CT.itemtype = R.id",
            "INNER JOIN players as P ON CT.playerid = P.id",
            "INNER JOIN containers as C ON CT.containerid = C.id",
            "INNER JOIN registry as DT ON DT.id = C.dimensionid",
            "WHERE C.x=? AND C.y=? AND C.z=? AND DT.name = ?",
            "ORDER BY CT.date DESC LIMIT ?"
          ),
          pos.getX(), pos.getY(), pos.getZ(), dimension.toString(), limit
        )
        .mapTo(Transaction.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Transaction> getTransactionsFromUUID(UUID uuid, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
        .select(
          String.join(" ",
            "SELECT C.uuid, CT.date, R.name as `itemname`, CT.itemcount, P.name as `playername`",
            "FROM container_transactions as CT",
            "INNER JOIN registry as R ON CT.itemtype = R.id",
            "INNER JOIN players as P ON CT.playerid = P.id",
            "INNER JOIN containers as C ON CT.containerid = C.id",
            "WHERE C.uuid=?",
            "ORDER BY CT.date DESC LIMIT ?"
          ),
          uuid.toString(), limit
        )
        .mapTo(Transaction.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public void processOps(List<QueueOperation> ops) {
    try {
      jdbi.useHandle(handle -> {
  
        if (ops.size() == 1) {
          // System.out.println("EXECUTE " + ops.get(0).getClass());
          ops.get(0).execute(handle);
          return;
        }
  
        PreparedBatch batch = ops.get(0).prepareBatch(handle);
        QueueOperation op = ops.get(0);
        op.addBindings(batch);
        int i = 1;
        while (i < ops.size()) {
          QueueOperation thisOp = ops.get(i);
  
          try {
            if (op.getClass() != thisOp.getClass()) {
              // Reached end or different operation, execute previous batch and/or create new batch
              // System.out.println("EXECUTE " + op.getClass());
              batch.execute();
              batch.close();
              batch = thisOp.prepareBatch(handle);
            }
            thisOp.addBindings(batch);
            if (i == ops.size() - 1) {
              // System.out.println("EXECUTE " + op.getClass());
              batch.execute();
              batch.close();
            }
          } catch (Exception e) {
            LOG.warn("Problem executing batches in handler");
            e.printStackTrace();
          }
          i++;
          op = thisOp;
        }
      });
    } catch (Exception e) {
      LOG.warn("Problem opening handle or something");
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    running.set(true);
    while (running.get()) {
      try {
        ArrayList<QueueOperation> queued = new ArrayList<>(50);
        Queues.drain(pq, queued, 50, 5000, TimeUnit.MILLISECONDS);
    
        if (queued.isEmpty()) continue;
        processOps(queued);
      } catch (InterruptedException e) {
        LOG.info("Stopping WatchTower");
        e.printStackTrace();
      }
    }
    tryToFinish();
  }

  public void tryToFinish() {
    ArrayList<QueueOperation> queued = new ArrayList<>(50);
    pq.drainTo(queued);
    if (queued.isEmpty()) return;
    LOG.info("WatchTower: Processing leftover database operations...");
    processOps(queued);
  }

  public void stop() {
    running.set(false);
  }
}