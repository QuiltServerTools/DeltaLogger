package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import com.github.fabricservertools.deltalogger.dao.ContainerDAO;
import com.github.fabricservertools.deltalogger.events.EntityDeathCallback;
import com.github.fabricservertools.deltalogger.events.PlayerOpenScreenCallback;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;
import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
  public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
    super(world, pos, yaw, profile);
  }

  @Inject(at = @At(value = "RETURN", ordinal = 2), method = "openHandledScreen", cancellable = true)
  public void openHandledScreen(
    NamedScreenHandlerFactory nameableContainerFactory,
    CallbackInfoReturnable<OptionalInt> info
  ) {
    ActionResult result = PlayerOpenScreenCallback.EVENT.invoker().openScreen((ServerPlayerEntity) (Object) this, nameableContainerFactory);

    if (result != ActionResult.PASS) {
      info.cancel();
    }
  }
}