package dev.lambdacraft.watchtower.mixins;

import java.util.OptionalInt;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.IWatchTowerId;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
  public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
    super(world, pos, yaw, profile);
  }

  @Inject(at = @At(value = "RETURN", ordinal = 2), method = "openHandledScreen")
  public void openHandledScreen(
    NamedScreenHandlerFactory nameableContainerFactory,
    CallbackInfoReturnable<OptionalInt> info
  ) {
      if (nameableContainerFactory instanceof LockableContainerBlockEntity && !(nameableContainerFactory instanceof ChestBlockEntity)) {
        BlockEntity be = (BlockEntity) nameableContainerFactory;
        UUID uuid = ((IWatchTowerId)be).getWatchTowerId();
        ((IWatchTowerId)this.currentScreenHandler).setWatchTowerId(uuid);

        Identifier blockId = Registry.BLOCK.getId(be.getCachedState().getBlock());
        DatabaseManager.getSingleton().queueOp(new DatabaseManager.ContainerUpdate(
          uuid, blockId, be.getPos(), this.getUuid(), DatabaseManager.getTime(), null
        ));
      }
  }
}