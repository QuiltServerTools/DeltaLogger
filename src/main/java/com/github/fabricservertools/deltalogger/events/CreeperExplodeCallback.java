package com.github.fabricservertools.deltalogger.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.ActionResult;

public interface CreeperExplodeCallback {
	Event<CreeperExplodeCallback> EVENT = EventFactory.createArrayBacked(CreeperExplodeCallback.class,
			(listeners) -> creeper -> {
				for (CreeperExplodeCallback listener : listeners) {
					ActionResult result = listener.explode(creeper);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			});

	ActionResult explode(CreeperEntity creeper);
}
