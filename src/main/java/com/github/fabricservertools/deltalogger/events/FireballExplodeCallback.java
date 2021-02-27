package com.github.fabricservertools.deltalogger.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;

public interface FireballExplodeCallback {
	Event<FireballExplodeCallback> EVENT = EventFactory.createArrayBacked(FireballExplodeCallback.class,
			(listeners) -> (creeper, hitResult) -> {
				for (FireballExplodeCallback listener : listeners) {
					ActionResult result = listener.explode(creeper, hitResult);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			});

	ActionResult explode(FireballEntity creeper, HitResult hitResult);
}
