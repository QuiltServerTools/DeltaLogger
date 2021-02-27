package com.github.fabricservertools.deltalogger;

import com.github.fabricservertools.deltalogger.command.Commands;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;
import com.github.fabricservertools.deltalogger.dao.PlayerDAO;
import com.github.fabricservertools.deltalogger.dao.RegistryDAO;
import com.github.fabricservertools.deltalogger.gql.ApiServer;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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

	private void onServerSetup(MinecraftServer server) {
		dm = DatabaseManager.create(server.getSavePath(WorldSavePath.ROOT).toFile());

		String portString = CONFIG.getProperty("webapp_port", "8080");
		try {
			apiServer.start(Integer.parseInt(portString));
		} catch (NumberFormatException | NullPointerException e) {
			throw new RuntimeException("invalid port number: " + portString);
		}
	}

	private void onServerStart(MinecraftServer server) {
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

	private void onStop(MinecraftServer server) {
		apiServer.stop();
		dm.stop();
		try {
			dmThread.join();
		} catch (InterruptedException e) {
			DeltaLogger.LOG.warn("Interrupted while writing to database");
		}
	}

	private void onJoin(ServerPlayNetworkHandler networkHandler, PacketSender sender, MinecraftServer server) {
		DatabaseManager.getSingleton()
				.queueOp(PlayerDAO.insert(
						networkHandler.player.getUuid(),
						networkHandler.player.getName().asString(),
						java.time.Instant.now()
				));
	}

	private void onQuit(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
		DatabaseManager.getSingleton()
				.queueOp(PlayerDAO.insert(
						networkHandler.player.getUuid(),
						networkHandler.player.getName().asString(),
						java.time.Instant.now()
				));
	}

	private void onBreakFinished(World world, PlayerEntity player, BlockPos pos,
								 BlockState state, /* Nullable */ BlockEntity blockEntity) {
		Identifier id = Registry.BLOCK.getId(state.getBlock());
		Identifier dimension = world.getRegistryKey().getValue();

		DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
				player.getUuid(),
				id,
				false,
				pos,
				world.getBlockState(pos),
				dimension,
				java.time.Instant.now()
		));
	}

	@Override
	public void onInitialize() {
		loadConfig(Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "deltalogger.properties"));

		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerSetup);
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onStop);

		ServerPlayConnectionEvents.JOIN.register(this::onJoin);
		ServerPlayConnectionEvents.DISCONNECT.register(this::onQuit);

		PlayerBlockBreakEvents.AFTER.register(this::onBreakFinished);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Commands.register(dispatcher));
	}
}
