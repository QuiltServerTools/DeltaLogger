package com.github.fabricservertools.deltalogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.github.fabricservertools.deltalogger.command.Commands;
import com.github.fabricservertools.deltalogger.dao.RegistryDAO;
import com.github.fabricservertools.deltalogger.gql.ApiServer;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.WorldSavePath;

public class ModInit implements ModInitializer {
	private static DatabaseManager dm;
	public static final Logger LOG = LogManager.getLogger();
	public static Properties CONFIG;
	public static Thread dmThread;

	public static Properties loadConfig(Path configPath) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configPath.toString()));
		} catch (FileNotFoundException e) {
			props.setProperty("use_sqlite", "true");

			props.setProperty("host", "");
			props.setProperty("port", "3306");
			props.setProperty("username", "");
			props.setProperty("password", "");
			props.setProperty("database", "");
			props.setProperty("useSSL", "true");
			props.setProperty("requireSSL", "false");
			props.setProperty("maxLifetime", "290000");

			try {
				props.store(new FileWriter(configPath.toString()), "Config for DeltaLogger");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			LOG.info("Optional configuration for DeltaLogger created in `config` directory. Using SQLite by default.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		CONFIG = props;
		return CONFIG;
	}

	@Override
	public void onInitialize() {
		loadConfig(Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "deltalogger.properties"));

		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			dm = DatabaseManager.create(server.getSavePath(WorldSavePath.ROOT).toFile());
			ApiServer.start();

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
	
			Set<Identifier> ids = idSets.stream().reduce((acc, s) -> Sets.union(acc, s)).orElse(new HashSet<Identifier>());

			ids.forEach(id -> dm.queueOp(RegistryDAO.insert(id)));

			ModInit.dmThread = new Thread(dm);
			ModInit.dmThread.start();
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			Commands.register(dispatcher);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
			dm.stop();
			for (int i = 1; i <= 2 * 30 && ModInit.dmThread.isAlive(); ++i) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (i == 60) {
					LOG.warn("WatchTower: Taking too long to finish up queue!");
					ModInit.dmThread.interrupt();
				}
			}
		});
	}
}
