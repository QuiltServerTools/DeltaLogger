package dev.lambdacraft.watchtower.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lambdacraft.watchtower.ITransactable;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class WindowTrackerMixin implements ServerPlayPacketListener {
  @Shadow
  public ServerPlayerEntity player;

  @Inject(
    at = @At(value = "INVOKE", target = "net.minecraft.screen.ScreenHandler.sendContentUpdates()V"),
    method = "onClickWindow")
  public void onClickWindow(ClickWindowC2SPacket packet, CallbackInfo info) {
    ((ITransactable)this.player.currentScreenHandler).trackLatestTransactions();
  }
}