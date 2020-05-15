package dev.lambdacraft.watchtower.mixins;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.beans.Player;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class PlayerEventsMixin {
  @Inject(at = @At("TAIL"), method = "remove")
  public void remove(ServerPlayerEntity player, CallbackInfo info) {
    DatabaseManager.getSingleton().queuePlayerUpdate(new Player(
      player.getUuid(),
      player.getName().asString(),
      DatabaseManager.getTime()
    ));
  }

  @Inject(at = @At("TAIL"), method = "onPlayerConnect")
  public void onPlayerConnect(ClientConnection connection,
    ServerPlayerEntity player, CallbackInfo info
  ) {
    DatabaseManager.getSingleton().queuePlayerUpdate(new Player(
      player.getUuid(),
      player.getName().asString(),
      DatabaseManager.getTime()
    ));
  }
}