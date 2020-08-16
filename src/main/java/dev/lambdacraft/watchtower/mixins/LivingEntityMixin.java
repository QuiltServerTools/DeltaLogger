package dev.lambdacraft.watchtower.mixins;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lambdacraft.watchtower.DatabaseManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Log name tagged entity death reason */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

  public LivingEntityMixin(EntityType<?> type, World world) {
    super(type, world);
  }

  @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;"), method = "onDeath")
  public void onDeath(DamageSource source, CallbackInfo info) {
    LivingEntity me = (LivingEntity)(Object)this;
    Entity attacker = source.getAttacker();
    if (me.getCustomName() == null) return;

    UUID killerid = null;
    if (attacker != null && attacker instanceof PlayerEntity) {
      killerid = ((PlayerEntity)attacker).getUuid();
    }

    DatabaseManager.getSingleton().queueOp(new DatabaseManager.EntityKillUpdate(
      me.getCustomName().asString(),
      source.getName(),
      killerid,
      DatabaseManager.getTime(),
      me.getBlockPos()
    ));
  }
}