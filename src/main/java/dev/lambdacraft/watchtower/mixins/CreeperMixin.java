package dev.lambdacraft.watchtower.mixins;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.registry.Registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lambdacraft.watchtower.DatabaseManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Log creeper explosion events and the creeper's player target */
@Mixin(CreeperEntity.class)
public class CreeperMixin extends HostileEntity {
  protected CreeperMixin(EntityType<? extends HostileEntity> type, World world) {
    super(type, world);
  }

  @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/CreeperEntity;explode()V"))
  private void tryLogCreeperExplode(CallbackInfo info) {
    LivingEntity target = this.getTarget();
    if (target instanceof PlayerEntity) {
      PlayerEntity playerTarget = (PlayerEntity)target;
      
      DatabaseManager.getSingleton().queueOp(new DatabaseManager.MobGriefUpdate(
        playerTarget.getUuid(),
        DatabaseManager.getTime(),
        Registry.ENTITY_TYPE.getId(EntityType.CREEPER),
        this.getBlockPos()
      ));
    }
  }
}
