package com.github.fabricservertools.deltalogger.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.KilledEntity;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EntityDAO {
  private Jdbi jdbi;
  public EntityDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(KilledEntity.class, (rs, ctx) -> {
      return new KilledEntity(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("source"),
        rs.getString("killer"),
        rs.getString("dimension"),
        rs.getString("date"),
        rs.getInt("x"),
        rs.getInt("y"),
        rs.getInt("z")
      );
    });
  }

  public List<KilledEntity> getLatestKilledEntities(int idOffset, int limit) {
    return jdbi.withHandle(handle -> handle
      .select(String.join(" ",
        "SELECT KE.id, KE.name, source, PL.name as killer, DR.name as dimension, date, x, y, z",
        "FROM (SELECT * FROM killed_entities WHERE id <",
          SQLUtils.offsetOrZeroLatest("killed_entities", "id", idOffset),
        "ORDER BY `id` DESC LIMIT ?) as KE",
        "LEFT JOIN players as PL ON KE.killer_id = PL.id",
        "LEFT JOIN registry as DR ON KE.dimension_id = DR.id"
      ), limit)
      .mapTo(KilledEntity.class)
      .list()
    );
  }

  public static QueueOperation insertMobGrief(
    UUID target,
    Instant date,
    Identifier entityType,
    BlockPos pos,
    Identifier dimensionId
  ) {
    return new QueueOperation() {
  
      public PreparedBatch prepareBatch(Handle handle) {
        return handle.prepareBatch(String.join("",
          "INSERT INTO mob_grief (date, entity_type, target, x, y, z, dimension_id) ",
          "SELECT :date, registry.id, players.id, :x, :y, :z,",
            "(SELECT id FROM registry WHERE registry.name=:dimension)",
          "FROM players, registry ",
          "WHERE players.uuid=:target AND registry.name=:entityType "
        ));
      }
      public PreparedBatch addBindings(PreparedBatch batch) {
        return batch
          .bind("date", SQLUtils.instantToUTCString(date))
          .bind("target", target.toString())
          .bind("entityType", entityType.toString())
          .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
          .bind("dimension", dimensionId.toString())
          .add();
      }
  
      public int getPriority() {
        return 2;
      }
    };
  }

  public static QueueOperation insertKill(
    String name,
    String sourceName,
    UUID killer_id,
    Instant date,
    BlockPos pos,
    Identifier dimId
  ) {
    return new QueueOperation() {
      public int getPriority() { return 2; }
  
      public PreparedBatch prepareBatch(Handle handle) {
        return handle.prepareBatch(String.join("",
          "INSERT INTO killed_entities (name, source, killer_id, date, x, y, z, dimension_id) ",
          "VALUES (:name, :source, ",
            "(CASE WHEN :killer_id IS NULL THEN NULL ELSE (SELECT id FROM players WHERE uuid=:killer_id) END), ",
            ":date, :x, :y, :z, (SELECT `id` FROM registry WHERE `name`=:dimension))"
        ));
      }
      public PreparedBatch addBindings(PreparedBatch batch) {
        return batch
          .bind("name", name)
          .bind("source", sourceName)
          .bind("killer_id", killer_id != null ? killer_id.toString() : null)
          .bind("date", SQLUtils.instantToUTCString(date))
          .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
          .bind("dimension", dimId.toString())
          .add();
      }
    };
  }
}
