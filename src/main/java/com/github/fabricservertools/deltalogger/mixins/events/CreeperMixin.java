package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.EntityDAO;
import com.github.fabricservertools.deltalogger.events.CreeperExplodeCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Log creeper explosion events and the creeper's player target
 */
@Mixin(CreeperEntity.class)
public class CreeperMixin extends HostileEntity {
	protected CreeperMixin(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}

	@Inject(method = "explode", at = @At(value = "HEAD"), cancellable = true)
	private void dlExplodeEventTrigger(CallbackInfo info) {
		ActionResult result = CreeperExplodeCallback.EVENT.invoker().explode((CreeperEntity) (Object) this);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}
}
