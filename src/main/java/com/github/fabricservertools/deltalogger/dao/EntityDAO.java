package com.github.fabricservertools.deltalogger.dao;

import java.time.Instant;
import java.util.UUID;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EntityDAO {
  private Jdbi jdbi;
  public EntityDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public static QueueOperation insertMobGrief(
    UUID target,
    Instant date,
    Identifier entityType,
    BlockPos pos
  ) {
    return new QueueOperation() {
  
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
          .bind("date", SQLUtils.instantToUTCString(date))
          .bind("target", target.toString())
          .bind("entityType", entityType.toString())
          .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
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
