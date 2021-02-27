package com.github.fabricservertools.deltalogger.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

public interface EntityDeathCallback {
	Event<EntityDeathCallback> EVENT = EventFactory.createArrayBacked(EntityDeathCallback.class,
			(listeners) -> (entity, damageSource) -> {
				for (EntityDeathCallback listener : listeners) {
					ActionResult result = listener.death(entity, damageSource);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			});

	ActionResult death(LivingEntity entity, DamageSource damageSource);
}
