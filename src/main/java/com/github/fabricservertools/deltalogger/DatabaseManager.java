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
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlite3.SQLitePlugin;

/**
 * Handles inserting QueueOperations into the database. All QOs are inserted
 * into a queue and pulled in order of priority. This is to ensure that foreign
 * table references are processed in the correct order, otherwise foreign key
 * constraints would fail. Foreign key constraints have been removed from the
 * SQL schema because shared host providers use outdated MySQL versions that
 * lack foreign key constraints.
 */
public class DatabaseManager implements Runnable {
  private static DatabaseManager manager;

  public enum DatabaseType {
    SQLITE("SQLite"), MYSQL("MySQL");

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
  private static final boolean isDevelop;
  static {
    String dev = System.getProperty("develop");
    isDevelop = dev != null && dev.equals("true");
  }
  public static DatabaseType dbType;

  public boolean isMysql() {
    return dbType == DatabaseType.MYSQL;
  }

  public boolean isSqlite() {
    return dbType == DatabaseType.SQLITE;
  }

  private PriorityBlockingQueue<QueueOperation> pq = new PriorityBlockingQueue<>(10,
      Comparator.comparingInt(QueueOperation::getPriority));
  private final AtomicBoolean running = new AtomicBoolean(false);
  public static final Logger LOG = LogManager.getLogger();

  private DatabaseManager(File worldSavePath) {
    connect(worldSavePath);
    DAO.register(jdbi);
  }

  public static DatabaseManager create(File worldSavePath) {
    if (manager != null)
      throw new Error("Only one DB manager should exist at a time!");
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

      checkValidSchema();
      if (isDevelop) {
        jdbi.setSqlLogger(new SqlLogger() {
          public void logAfterExecution(StatementContext context) {
            LOG.info(context.getRenderedSql());
            LOG.info(context.getBinding().toString());
          }
        });
      }

      LOG.info("DeltaLogger started with " + dbType + " database");
    } catch (IOException e) {
      e.printStackTrace();
      // System.exit(0);
    }
  }

  private Jdbi initJdbiSQLite(File worldSavePath) {
    try {
      File databaseFile = worldSavePath != null ? new File(worldSavePath, "deltalogger.sqlite")
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
    config.setJdbcUrl(String.join("", "jdbc:mysql://", props.getProperty("host"), ":", props.getProperty("port"), "/",
        props.getProperty("database")));
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

  public void runScript(String path) throws IOException {
    String sql = preproccessSQL(
      Resources.toString(
        DatabaseManager.class.getResource(path), 
        StandardCharsets.UTF_8
      )
    );
    jdbi.useHandle(handle -> {
      handle.createScript(sql).execute();
    });
  }

  public void checkValidSchema() throws IOException {
    LOG.info("Checking for valid schema..");
    runScript("/data/watchtower/kv_store_table.sql");

    String sver;
    try {
      sver = jdbi.withHandle(handle -> handle
        .createQuery("SELECT `value` FROM kv_store WHERE `key` = 'schema_version'")
        .mapTo(String.class)
        .findOne()
      ).orElse("0");
    } catch (UnableToExecuteStatementException e) {
      sver = "0";
    }

    int version;
    try {
      version = Integer.parseInt(sver);
    } catch (Exception e) {
      throw new Error("Invalid schema version");
    }

    if (version == 0) {
      if (isMysql()) {
        String migrateSql = Resources.toString(
            DatabaseManager.class.getResource("/data/watchtower/migration/wt_to_bl.sql"), StandardCharsets.UTF_8);
        LOG.info(String.join("\n", "",
          "Migrating WatchTower database to DeltaLogger database.",
          "This may take a few minutes to a few hours depending on the size of your database.",
          "This will start in 60 seconds. IF YOU WISH TO CANCEL THEN EXIT NOW."
        ));
        try {
          Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
          throw new Error("Cancelling pending migration...");
        }
        LOG.info("Starting database migration now, do not exit!");
        
        jdbi.withHandle(handle -> handle
          .createScript(migrateSql)
          .execute()
        );

        jdbi.withHandle(handle -> handle
          .createUpdate(String.join(" ",
            "INSERT INTO kv_store (`key`,`value`) VALUES ('schema_version', :version)",
            SQLUtils.onDuplicateKeyUpdate("schema_version"), "`value`=:version"
          ))
          .bind("version", "1")
          .execute()
        );

        LOG.info("Database migration completed.");
      }
      // TODO migrate blocklogger sqlite
    }

    runScript("/data/watchtower/schema.sql");
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
        LOG.info("Stopping DeltaLogger");
        e.printStackTrace();
      }
    }
    tryToFinish();
  }

  public void tryToFinish() {
    ArrayList<QueueOperation> queued = new ArrayList<>(50);
    pq.drainTo(queued);
    if (queued.isEmpty()) return;
    LOG.info("DeltaLogger: Processing leftover database operations...");
    processOps(queued);
  }

  public void stop() {
    running.set(false);
  }
}