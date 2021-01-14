package com.github.fabricservertools.deltalogger.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.util.PlayerUtils;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BlockDAO {
  private Jdbi jdbi;
  public static final String SELECT_PLACEMENT = String.join(" ",
    "SELECT PL.id, P.name AS `player_name`,",
    SQLUtils.getDateFormatted("date"),
    ", IT.name AS `block_type`, x, y, z, placed, DT.name as `dimension`"
  );
  private final String JOIN_PLACEMENT =  String.join(" ",
    "INNER JOIN players as P ON P.id=player_id",
    "INNER JOIN registry as IT ON IT.id=type",
    "INNER JOIN registry as DT ON DT.id=dimension_id"
  );

  public BlockDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(Placement.class, (rs, ctx) -> {
      return new Placement(
        rs.getInt("id"),
        rs.getString("player_name"),
        rs.getString("date"),
        rs.getString("block_type"),
        rs.getInt("x"), rs.getInt("y"), rs.getInt("z"),
        rs.getBoolean("placed"),
        rs.getString("dimension")
      );
    });
  }

  /**
   * Get latest placements
   * @param idOffset must be the id of the row to offset from, if offset is 0 then get latest
   * @param limit the number of rows to return
   * @return
   */
  public List<Placement> search(Identifier dimension, int idOffset, int limit, String builtWhere) {
    try {
      return jdbi.withHandle(handle -> handle
        .createQuery(
          String.join(" ",
            SELECT_PLACEMENT,
            "FROM (",
              "SELECT * FROM placements",
              "WHERE placements.id < ", SQLUtils.offsetOrZeroLatest("placements", "placements.id", idOffset),
              builtWhere,
              "ORDER BY `id` DESC LIMIT :lim",
            ") as PL",
            JOIN_PLACEMENT
          )
        )
        .bind("lim", limit)
        .mapTo(Placement.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
  public List<Placement> customQuery(String sql) {
    return jdbi.withHandle(handle -> handle.select(sql, 100)).mapTo(Placement.class).list();
  }
  public List<Placement> getLatestPlacements(int idOffset, int limit) {
    return jdbi.withHandle(handle -> handle
      .select(String.join(" ",
        SELECT_PLACEMENT,
        "FROM (SELECT * FROM placements WHERE id <",
          SQLUtils.offsetOrZeroLatest("placements", "id", idOffset), // sql perf optim.
          "ORDER BY `id` DESC LIMIT ?) as PL",
        JOIN_PLACEMENT
      ), limit)
      .mapTo(Placement.class)
      .list()
    );
  }
  public List<Placement> rollbackQuery(Identifier dimension, BlockPos pos, int idOffset, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
        .createQuery(
          String.join(" ",
            SELECT_PLACEMENT,
            "FROM (",
              "SELECT * FROM placements",
              "WHERE placements.id < ", SQLUtils.offsetOrZeroLatest("placements", "placements.id", idOffset),
              "AND x = :x AND y = :y AND z = :z AND dimension_id = (SELECT id FROM registry WHERE `name` = :dim)",
              "ORDER BY `id` DESC LIMIT :lim",
            ") as PL",
            JOIN_PLACEMENT
          )
        )
        .bind("x", pos.getX()).bind("y", pos.getY()).bind("z", pos.getZ())
        .bind("dim", dimension.toString())
        .bind("lim", limit)
        .mapTo(Placement.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  /**
   * Get latest placements in dim at coords
   * @param idOffset must be the id of the row to offset from, if offset is 0 then get latest
   * @param limit the number of rows to return
   * @return
   */
  public List<Placement> getLatestPlacementsAt(Identifier dimension, BlockPos pos, int idOffset, int limit) {
    try {
      return jdbi.withHandle(handle -> handle
        .createQuery(
          String.join(" ",
            SELECT_PLACEMENT,
            "FROM (",
              "SELECT * FROM placements",
              "WHERE placements.id < ", SQLUtils.offsetOrZeroLatest("placements", "placements.id", idOffset),
              "AND x = :x AND y = :y AND z = :z AND dimension_id = (SELECT id FROM registry WHERE `name` = :dim)",
              "ORDER BY `id` DESC LIMIT :lim",
            ") as PL",
            JOIN_PLACEMENT
          )
        )
        .bind("x", pos.getX()).bind("y", pos.getY()).bind("z", pos.getZ())
        .bind("dim", dimension.toString())
        .bind("lim", limit)
        .mapTo(Placement.class).list()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
  public static QueueOperation insertPlacement(
    UUID player_id,
    Identifier blockid,
    boolean placed,
    BlockPos pos,
    Identifier dimension_id,
    Instant date
  ) {
    return new QueueOperation() {
      public int getPriority() { return 2; }
  
      public PreparedBatch prepareBatch(Handle handle) {
        return handle.prepareBatch(String.join(" ",
          "INSERT INTO placements (date, placed, x, y, z, player_id, type, dimension_id)",
          "SELECT :date, :placed, :x, :y, :z,",
            "(SELECT id FROM players WHERE uuid=:playeruuid),",
            "(SELECT id FROM registry WHERE name=:blockid),",
            "(SELECT id FROM registry WHERE name=:dimension_id)"
        ));
      }
  
      public PreparedBatch addBindings(PreparedBatch batch) {
        return batch
          .bind("date", SQLUtils.instantToUTCString(date))
          .bind("placed", placed)
          .bind("x", pos.getX()).bind("y", pos.getY()).bind("z", pos.getZ())
          .bind("playeruuid", player_id.toString())
          .bind("blockid", blockid.toString())
          .bind("dimension_id", dimension_id.toString())
          .add();
      }
    };
  }
}
