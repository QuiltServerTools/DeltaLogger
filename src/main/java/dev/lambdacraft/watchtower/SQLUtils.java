package dev.lambdacraft.watchtower;

import static dev.lambdacraft.watchtower.DatabaseManager.DatabaseType;
import static dev.lambdacraft.watchtower.DatabaseManager.dbType;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SQLUtils {
  public static DateTimeFormatter timeFormat;
  static {
    timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
  }
  /**
   * Preproccess SQL string script so that it is compatible between database types
   */
  public static String preproccessSQL(String sql) {
    String primaryKeyType = dbType == DatabaseType.MYSQL ? "INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT" : "INTEGER PRIMARY KEY";
    String smallPrimaryKeyType = dbType == DatabaseType.MYSQL ? "SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT" : "INTEGER PRIMARY KEY";
    String bigPrimaryKeyType = dbType == DatabaseType.MYSQL ? "BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT" : "INTEGER PRIMARY KEY";
    return sql
      .replace("/**!PRIMARY_KEY*/", primaryKeyType)
      .replace("/**!SMALL_PRIMARY_KEY*/", smallPrimaryKeyType)
      .replace("/**!BIG_PRIMARY_KEY*/", bigPrimaryKeyType);
  }

  /**
   * SQL query to perform an update when there is already a duplicate key
   * @param key duplicate key
   * @return
   */
  public static String onDuplicateKeyUpdate(String key) {
    switch (dbType) {
      case MYSQL:
        return "ON DUPLICATE KEY UPDATE ";
      case SQLITE:
        return String.join("", "ON CONFLICT(", key, ") DO UPDATE SET ");
      default:
        throw new Error("Unhandled DB type");
    }
  }

  /**
   * Overload getDateFormatted for optional columnName
   * @param date
   * @return
   */
  public static String getDateFormatted(String date) {
    return getDateFormatted(date, null);
  }

  /**
   * Cross-db SQL query to get time as a string in SQLUtils::timeFormat
   * @param date
   * @param columnName optional column name for use in AS sql part
   * @return
   */
  public static String getDateFormatted(String date, String columnName) {
    String _columnName = columnName == null ? date : columnName;
    switch (dbType) {
      case MYSQL:
        return "DATE_FORMAT(" + date + ", \"%Y-%m-%d %T\") as `" + _columnName + "`";
      case SQLITE:
        return "DATETIME(" + date + ") as `" + _columnName + "`";
      default:
        throw new Error("Unhandled DB type");
    }
  }

  /**
   * Convert DB's time string (like from getDateFormatted) to an Instant which
   * represents an instant in time zoned to UTC
   * @param dbTimeString
   * @return
   */
  public static Instant getInstantFromDBTimeString(String dbTimeString) {
    return timeFormat.parse(dbTimeString, Instant::from);
  }

  /**
   * Get a now time formatted to a string via SQLUtils::timeFormat 
   * @return
   */
  public static String getUTCStringTimeNow() {
    return timeFormat.format(java.time.Instant.now());
  }
}
