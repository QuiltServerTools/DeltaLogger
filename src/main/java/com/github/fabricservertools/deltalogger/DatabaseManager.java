package com.github.fabricservertools.deltalogger;

import static com.github.fabricservertools.deltalogger.SQLUtils.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import com.github.fabricservertools.deltalogger.dao.DAO;
import com.google.common.collect.Queues;
import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.sqlite3.SQLitePlugin;

/**
 * Handles inserting QueueOperations into the database. All QOs are inserted into a
 * queue and pulled in order of priority. This is to ensure that foreign table
 * references are processed in the correct order, otherwise foreign key
 * constraints would fail. Foreign key constraints have been removed from the
 * SQL schema because shared host providers use outdated MySQL versions that
 * lack foreign key constraints.
 */
public class DatabaseManager implements Runnable {
  private static DatabaseManager manager;

  public enum DatabaseType {
    SQLITE("SQLite"),
    MYSQL("MySQL");

    private final String s;
    DatabaseType(String s) {
      this.s = s;
    }
    @Override
    public String toString() {
      return s;
    }
  }

  private Jdbi jdbi;
  public static DatabaseType dbType;
  
  private PriorityBlockingQueue<QueueOperation> pq = new PriorityBlockingQueue<>(10, Comparator.comparingInt(QueueOperation::getPriority));
  private final AtomicBoolean running = new AtomicBoolean(false);
  public static final Logger LOG = LogManager.getLogger();

  private DatabaseManager(File worldSavePath) {
    connect(worldSavePath);
    DAO.register(jdbi);
  }

  public static DatabaseManager create(File worldSavePath) {
    if (manager != null) throw new Error("Only one DB manager should exist at a time!");
    manager = new DatabaseManager(worldSavePath);
    return manager;
  }

  private void connect(File worldSavePath) {
    boolean useSqlite = ModInit.CONFIG.getProperty("use_sqlite").equals("true");
    try {
      if (useSqlite && worldSavePath != null) {
        dbType = DatabaseType.SQLITE;
        initJdbiSQLite(worldSavePath);
      } else if (useSqlite) {
        dbType = DatabaseType.SQLITE;
        initJdbiSQLite(null);
      } else {
        dbType = DatabaseType.MYSQL;
        initJdbiMySQL(getMySQLDataSource());
      }
      createTables();
      
      LOG.info("DeltaLogger started with " + dbType + " database");
    } catch (IOException e) {
      e.printStackTrace();
      // System.exit(0);
    }
  }

  private Jdbi initJdbiSQLite(File worldSavePath) {
    try {
      File databaseFile = worldSavePath != null
        ? new File(worldSavePath, "deltalogger.sqlite")
        : new File("./world/deltalogger.sqlite");
  
      jdbi = Jdbi.create("jdbc:sqlite:" + databaseFile.getCanonicalPath().replace('\\', '/'))
        .installPlugin(new SQLitePlugin());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jdbi;
  }

  private Jdbi initJdbiMySQL(DataSource source) throws IOException {
    jdbi = Jdbi.create(source);
    return jdbi;
  }

  public static DatabaseManager getSingleton() {
    return manager;
  }

  /**
   * Configure and init Hikari connection pool
   */
  private static HikariDataSource getMySQLDataSource() {
    HikariConfig config = new HikariConfig();
    Properties props = ModInit.CONFIG;
    config.setJdbcUrl(String.join("",
      "jdbc:mysql://",
      props.getProperty("host"), ":",
      props.getProperty("port"), "/",
      props.getProperty("database")
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
    String tableSql = preproccessSQL(
      Resources.toString(
        DatabaseManager.class.getResource("/data/watchtower/schema.sql"), 
        StandardCharsets.UTF_8
      )
    );
    jdbi.useHandle(handle -> {
      handle.createScript(tableSql).execute();
      // handle.execute("CREATE FUNCTION BIN_TO_UUID(b BINARY(16), f BOOLEAN) RETURNS CHAR(36) DETERMINISTIC BEGIN DECLARE hexStr CHAR(32); SET hexStr = HEX(b); RETURN LOWER(CONCAT(IF(f,SUBSTR(hexStr, 9, 8),SUBSTR(hexStr, 1, 8)), '-', IF(f,SUBSTR(hexStr, 5, 4),SUBSTR(hexStr, 9, 4)), '-', IF(f,SUBSTR(hexStr, 1, 4),SUBSTR(hexStr, 13, 4)), '-', SUBSTR(hexStr, 17, 4), '-', SUBSTR(hexStr, 21))); END;");
      // handle.execute("CREATE FUNCTION UUID_TO_BIN(uuid CHAR(36), f BOOLEAN) RETURNS BINARY(16) DETERMINISTIC BEGIN RETURN UNHEX(CONCAT(IF(f,SUBSTRING(uuid, 15, 4),SUBSTRING(uuid, 1, 8)),SUBSTRING(uuid, 10, 4),IF(f,SUBSTRING(uuid, 1, 8),SUBSTRING(uuid, 15, 4)),SUBSTRING(uuid, 20, 4),SUBSTRING(uuid, 25))); END;");
    });
  }

  public void queueOp(QueueOperation op) { pq.add(op); }

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