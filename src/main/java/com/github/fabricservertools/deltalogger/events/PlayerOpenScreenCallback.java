package com.github.fabricservertools.deltalogger.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerOpenScreenCallback {
	Event<PlayerOpenScreenCallback> EVENT = EventFactory.createArrayBacked(PlayerOpenScreenCallback.class,
			(listeners) -> (player, screenHandlerFactory) -> {
				for (PlayerOpenScreenCallback listener : listeners) {
					ActionResult result = listener.openScreen(player, screenHandlerFactory);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			});

	ActionResult openScreen(ServerPlayerEntity player, NamedScreenHandlerFactory screenHandlerFactory);
}
