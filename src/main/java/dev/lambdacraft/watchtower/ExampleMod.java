package dev.lambdacraft.watchtower;

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

import com.google.common.collect.Sets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {
	public static final Logger LOG = LogManager.getLogger();
	public static Properties CONFIG;
	public static Thread dmThread;

	public Properties loadConfig() {
		Path configPath = Paths.get(FabricLoader.getInstance().getConfigDirectory().toString(), "watchtower.properties");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configPath.toString()));
		} catch (FileNotFoundException e) {
			props.setProperty("host", "");
			props.setProperty("port", "3306");
			props.setProperty("username", "");
			props.setProperty("password", "");
			props.setProperty("database", "");
			props.setProperty("useSSL", "true");
			props.setProperty("requireSSL", "false");
			props.setProperty("maxLifetime", "290000");

			try {
				props.store(new FileWriter(configPath.toString()), "Config for WatchTower");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			LOG.warn("Created configuration file for WatchTower, please configure!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		CONFIG = props;
		return CONFIG;
	}

	@Override
	public void onInitialize() {
		DatabaseManager dm = DatabaseManager.init(loadConfig());

		ServerStartCallback.EVENT.register((server) -> {

			HashSet<Identifier> dimensionIds = new HashSet<>();
			server.getWorlds().forEach(world -> {
				Identifier dimid = world.getRegistryKey().getValue();
				System.out.println("FOUND DIM " + dimid);
				dimensionIds.add(dimid);
			});

			List<Set<Identifier>> idSets = new ArrayList<Set<Identifier>>() {{
				add(Registry.BLOCK.getIds());
				add(Registry.ITEM.getIds());
				add(Registry.ENTITY_TYPE.getIds());
				add(dimensionIds);
			}};
	
			Set<Identifier> ids = idSets.stream().reduce((acc, s) -> Sets.union(acc, s)).orElse(new HashSet<Identifier>());

			ids.forEach(dm::queueRegistryUpdate);

			ExampleMod.dmThread = new Thread(dm);
			ExampleMod.dmThread.start();
		});

    CommandRegistry.INSTANCE.register(false, dispatcher -> Commands.register(dispatcher));

		ServerStopCallback.EVENT.register((server) -> {
			dm.stop();
			for (int i = 1; i <= 2 * 30 && ExampleMod.dmThread.isAlive(); ++i) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (i == 60) {
					LOG.warn("WatchTower: Taking too long to finish up queue!");
					ExampleMod.dmThread.interrupt();
				}
			}
		});
	}
}
