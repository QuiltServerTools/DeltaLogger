package com.github.fabricservertools.deltalogger.mixins;

import java.util.UUID;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.EntityDAO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

  @Inject(
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;"
    ),
    method = "onDeath"
  )
  public void onDeath(DamageSource source, CallbackInfo info) {
    LivingEntity me = (LivingEntity)(Object)this;
    Entity attacker = source.getAttacker();
    if (me.getCustomName() == null) return;

    UUID killer_id = null;
    if (attacker != null && attacker instanceof PlayerEntity) {
      killer_id = ((PlayerEntity)attacker).getUuid();
    }

    DatabaseManager.getSingleton().queueOp(EntityDAO.insertKill(
      me.getCustomName().asString(),
      source.getName(),
      killer_id,
      java.time.Instant.now(),
      me.getBlockPos(),
      attacker.getEntityWorld().getRegistryKey().getValue()
    ));
  }
}