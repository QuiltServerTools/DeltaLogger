package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.EntityDAO;
import com.github.fabricservertools.deltalogger.events.BlockPlaceCallback;
import com.github.fabricservertools.deltalogger.events.EntityDeathCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Log name tagged entity death reason
 */
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
			method = "onDeath",
			cancellable = true
	)
	public void onDeath(DamageSource source, CallbackInfo info) {
		ActionResult result = EntityDeathCallback.EVENT.invoker().death((LivingEntity) (Object) this, source);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}
}