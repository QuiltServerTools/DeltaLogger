package dev.lambdacraft.watchtower.mixins;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lambdacraft.watchtower.DatabaseManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/** Log fireball explosions and the fireballing entity's target */
@Mixin(FireballEntity.class)

// onCollision
public class FireballEntityMixin extends AbstractFireballEntity {
  public FireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, LivingEntity livingEntity,
      double d, double e, double f, World world) {
    super(entityType, livingEntity, d, e, f, world);
  }

  @Inject(
    method = "onCollision",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
  private void tryLogFireballExplosion(HitResult result, CallbackInfo info) {
    Entity owner = this.getOwner();
    if (
      !this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)
      || !(owner instanceof GhastEntity)
      || owner == null
    ) return;
    GhastEntity ghast = ((GhastEntity)owner);
    if (ghast.getTarget() ==  null || !(ghast.getTarget() instanceof PlayerEntity)) return;

    DatabaseManager.getSingleton().queueOp(new DatabaseManager.MobGriefUpdate(
      ((PlayerEntity)ghast.getTarget()).getUuid(),
      DatabaseManager.getTime(),
      Registry.ENTITY_TYPE.getId(EntityType.GHAST),
      this.getBlockPos()
    ));
    
  }
  
}