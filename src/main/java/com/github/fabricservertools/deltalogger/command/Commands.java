package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.command.rollback.RollbackCommand;
import com.github.fabricservertools.deltalogger.command.search.SearchCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager.literal("dl")
				.requires(source -> source.hasPermissionLevel(3)).build();

		dispatcher.getRoot().addChild(deltaNode);

		InspectCommand.register(deltaNode);
		SearchCommand.register(deltaNode);
		ResetPassCommand.register(deltaNode);
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			SQLCommand.register(deltaNode);
		}
		InfoCommand.register(deltaNode);
		RollbackCommand.register(deltaNode);
	}

}
