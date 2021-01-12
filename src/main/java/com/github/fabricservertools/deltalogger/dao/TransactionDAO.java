package com.github.fabricservertools.deltalogger.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Transaction;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TransactionDAO {
  private Jdbi jdbi;
  private final String SELECT_TRANSACTIONS = String.join(" ", "SELECT CT.id, C.uuid,",
      SQLUtils.getDateFormatted("CT.date", "date"), ", R.name as `item_type`, CT.item_count, P.name as `player_name`");
  private final String JOIN_TRANSACTIONS = String.join(" ", "INNER JOIN registry as R ON CT.item_type = R.id",
      "INNER JOIN containers as C ON CT.container_id = C.id", "LEFT JOIN registry as DT ON DT.id = C.dimension_id",
      "INNER JOIN players as P ON CT.player_id = P.id");

  public TransactionDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(Transaction.class,
        (rs, ctx) -> new Transaction(rs.getInt("id"), rs.getString("player_name"), rs.getString("date"),
            rs.getString("item_type"), rs.getInt("item_count"), UUID.fromString(rs.getString("uuid"))));
  }

  public List<Transaction> getTransactions() {
    try {
      List<Transaction> tr = jdbi
          .withHandle(handle -> handle.createQuery(SELECT_TRANSACTIONS).mapTo(Transaction.class).list());

      return tr;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Transaction> getTransactionsAt(Identifier dimension, BlockPos pos, int limit) {
    try {
      return jdbi.withHandle(handle -> handle.select(
          String.join(" ", SELECT_TRANSACTIONS, "FROM container_transactions as CT", JOIN_TRANSACTIONS,
              "WHERE C.x=? AND C.y=? AND C.z=? AND DT.name = ?", "ORDER BY CT.date DESC LIMIT ?"),
          pos.getX(), pos.getY(), pos.getZ(), dimension.toString(), limit).mapTo(Transaction.class).list());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Transaction> search(Identifier dimension, int limit, String builtWhere) {
    try {
      return jdbi
          .withHandle(handle -> handle.select(String.join(" ", SELECT_TRANSACTIONS, "FROM container_transactions as CT",
              JOIN_TRANSACTIONS, builtWhere, "ORDER BY CT.date DESC LIMIT " + limit)).mapTo(Transaction.class).list());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Transaction> customQuery(String sql) {
    try {
      return jdbi.withHandle(handle -> handle.select(sql, 100)).mapTo(Transaction.class).list();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public List<Transaction> getTransactionsFromUUID(UUID uuid, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
          .select(String.join(" ", SELECT_TRANSACTIONS, "FROM container_transactions as CT", JOIN_TRANSACTIONS,
              "WHERE C.uuid=?", "ORDER BY CT.date DESC LIMIT ?"), uuid.toString(), limit)
          .mapTo(Transaction.class).list());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public static QueueOperation insert(UUID playerId, UUID containerId, Instant date, Identifier item_type,
      Integer item_count, byte[] item_data) {
    return new QueueOperation() {
      public int getPriority() {
        return 3;
      }

      public PreparedBatch prepareBatch(Handle handle) {
        return handle.prepareBatch(String.join("",
            "INSERT INTO container_transactions (player_id, container_id, date, item_type, item_count, item_data)",
            "SELECT players.id, containers.id, :date, registry.id, :item_count, :item_data ",
            "FROM players, containers, registry ",
            "WHERE players.uuid=:playerId AND containers.uuid=:containerId AND registry.name=:item_type "));
      }

      public PreparedBatch addBindings(PreparedBatch batch) {
        return batch.bind("playerId", playerId.toString()).bind("containerId", containerId.toString())
            .bind("date", SQLUtils.instantToUTCString(date)).bind("item_type", item_type.toString())
            .bind("item_count", item_count).bind("item_data", item_data).add();
      }
    };
  }
}
