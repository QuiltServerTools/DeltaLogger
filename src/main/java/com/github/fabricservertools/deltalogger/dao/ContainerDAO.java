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

public class ContainerDAO {
  private Jdbi jdbi;
  public ContainerDAO(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public static QueueOperation insert(
    UUID uuid,
    Identifier blockId,
    BlockPos pos,
    UUID accessingPlayer,
    Instant last_access,
    Identifier dimension
  ) {

    return new QueueOperation() {
      public int getPriority() { return 2; }
  
      public PreparedBatch prepareBatch(Handle handle) {
        return handle.prepareBatch(String.join(" ",
          "INSERT INTO containers (uuid, last_access, x, y, z, first_player_id, last_player_id, item_type, dimension_id)",
          "SELECT :uuid, :last_access, :x, :y, :z, players.id, players.id, registry.id, ",
          "(SELECT id FROM registry WHERE name=:dimension_id)",
          "FROM players, registry",
          "WHERE players.uuid=:player AND registry.name=:item_type",
          SQLUtils.onDuplicateKeyUpdate("uuid"), "last_access=:last_access, x=:x, y=:y, z=:z,",
          "last_player_id=(SELECT id FROM players WHERE players.uuid=:player)"
        ));
      }
  
      public PreparedBatch addBindings(PreparedBatch batch) {
        return batch
          .bind("uuid", uuid.toString())
          .bind("last_access", SQLUtils.instantToUTCString(last_access))
          .bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
          .bind("player", accessingPlayer.toString())
          .bind("item_type", blockId.toString())
          .bind("dimension_id", dimension.toString())
          .add();
      }
    };
  }
}
