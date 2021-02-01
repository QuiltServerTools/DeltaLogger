package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.PlayerDAO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

/** Hook to log player session */
@Mixin(PlayerManager.class)
public abstract class PlayerEventsMixin {
  @Inject(at = @At("TAIL"), method = "remove")
  public void remove(ServerPlayerEntity player, CallbackInfo info) {
    DatabaseManager.getSingleton()
      .queueOp(PlayerDAO.insert(
        player.getUuid(),
        player.getName().asString(),
        java.time.Instant.now()
      ));
  }

  @Inject(at = @At("TAIL"), method = "onPlayerConnect")
  public void onPlayerConnect(ClientConnection connection,
    ServerPlayerEntity player, CallbackInfo info
  ) {
    DatabaseManager.getSingleton()
      .queueOp(PlayerDAO.insert(
        player.getUuid(),
        player.getName().asString(),
        java.time.Instant.now()
      ));
  }
}