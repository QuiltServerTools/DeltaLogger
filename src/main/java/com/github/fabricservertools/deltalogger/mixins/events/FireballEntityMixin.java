package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.EntityDAO;
import com.github.fabricservertools.deltalogger.events.CreeperExplodeCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Log fireball explosions and the fireballing entity's target
 */
@Mixin(FireballEntity.class)

// onCollision
public class FireballEntityMixin extends AbstractFireballEntity {
	public FireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, LivingEntity livingEntity,
							   double d, double e, double f, World world) {
		super(entityType, livingEntity, d, e, f, world);
	}

	@Inject(
			method = "onCollision",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"),
			cancellable = true
	)
	private void tryLogFireballExplosion(HitResult hit, CallbackInfo info) {
		ActionResult result = CreeperExplodeCallback.EVENT.invoker().explode((CreeperEntity) (Object) this);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}
}