package com.github.fabricservertools.deltalogger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LogManager.getLogger().info("Loaded DeltaLogger (Client) v" + FabricLoader.getInstance().getModContainer("deltalogger").get().getMetadata().getVersion().getFriendlyString());
	}
}
