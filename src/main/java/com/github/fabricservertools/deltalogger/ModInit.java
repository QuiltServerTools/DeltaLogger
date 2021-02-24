package com.github.fabricservertools.deltalogger;

import com.github.fabricservertools.deltalogger.command.Commands;
import com.github.fabricservertools.deltalogger.dao.RegistryDAO;
import com.github.fabricservertools.deltalogger.gql.ApiServer;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ModInit implements ModInitializer {
	private static DatabaseManager dm;
	public static Properties CONFIG;
	public static Thread dmThread;
	private static ApiServer apiServer = new ApiServer();

	public static Properties loadConfig(Path configPath) {
		Properties props = new Properties();

		if (!configPath.toFile().exists()) {
			// Create default config
			try {
				Files.copy(ModInit.class.getResourceAsStream("/data/deltalogger/default_config.properties"), configPath);
				DeltaLogger.LOG.info("Optional configuration for DeltaLogger created in `config` directory. Using SQLite by default.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			props.load(new FileInputStream(configPath.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		CONFIG = props;
		return CONFIG;
	}

	/**
	 * Called from MinecraftDedicatedServer::setupServer
	 *
	 * @param server Server instance
	 */
	public static void onServerInit(MinecraftServer server) {
		dm = DatabaseManager.create(server.getSavePath(WorldSavePath.ROOT).toFile());

		String portString = CONFIG.getProperty("webapp_port", "8080");
		try {
			apiServer.start(Integer.parseInt(portString));
		} catch (NumberFormatException | NullPointerException e) {
			throw new RuntimeException("invalid port number: " + portString);
		}
	}

	public void afterWorldLoad(MinecraftServer server) {
		HashSet<Identifier> dimensionIds = new HashSet<>();
		server.getWorlds().forEach(world -> {
			Identifier dimid = world.getRegistryKey().getValue();
			dimensionIds.add(dimid);
		});

		List<Set<Identifier>> idSets = new ArrayList<Set<Identifier>>() {{
			add(Registry.BLOCK.getIds());
			add(Registry.ITEM.getIds());
			add(Registry.ENTITY_TYPE.getIds());
			add(dimensionIds);
		}};

		Set<Identifier> ids = idSets.stream().reduce(Sets::union).orElse(new HashSet<>());

		ids.forEach(id -> dm.queueOp(RegistryDAO.insert(id)));

		ModInit.dmThread = new Thread(dm);
		ModInit.dmThread.start();
	}

	public void onStop(MinecraftServer server) {
		apiServer.stop();
		dm.stop();
		try {
			dmThread.join();
		} catch (InterruptedException e) {
			DeltaLogger.LOG.warn("Interrupted while writing to database");
		}
	}

	@Override
	public void onInitialize() {
		loadConfig(Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "deltalogger.properties"));

		ServerLifecycleEvents.SERVER_STARTED.register(this::afterWorldLoad);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Commands.register(dispatcher));

		ServerLifecycleEvents.SERVER_STOPPED.register(this::onStop);
	}
}
