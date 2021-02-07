package com.github.fabricservertools.deltalogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.WorldSavePath;

public class ModInit implements ModInitializer {
  private static DatabaseManager dm;
  public static Properties CONFIG;
  public static Thread dmThread;

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

  public void onModInit() {
    loadConfig(Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "deltalogger.properties"));
  }

  /**
   * Called from MinecraftDedicatedServer::setupServer
   * @param server Server instnace
   */
  public static void onServerInit(MinecraftServer server) {
    dm = DatabaseManager.create(server.getSavePath(WorldSavePath.ROOT).toFile());
    ApiServer.start();
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

    Set<Identifier> ids = idSets.stream().reduce((acc, s) -> Sets.union(acc, s)).orElse(new HashSet<Identifier>());

    ids.forEach(id -> dm.queueOp(RegistryDAO.insert(id)));

    ModInit.dmThread = new Thread(dm);
    ModInit.dmThread.start();
  }

  public void onStop(MinecraftServer server) {
    dm.stop();
    for (int i = 1; i <= 2 * 30 && ModInit.dmThread.isAlive(); ++i) {
      try {
        // FIXME hangs on /stop command
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (i == 60) {
        DeltaLogger.LOG.warn("Taking too long to finish up queue!");
        ModInit.dmThread.interrupt();
      }
    }
  }
  

  @Override
  public void onInitialize() {
    onModInit();

    ServerLifecycleEvents.SERVER_STARTED.register(this::afterWorldLoad);

    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      Commands.register(dispatcher);
    });

    ServerLifecycleEvents.SERVER_STOPPED.register(this::onStop);
  }
}
