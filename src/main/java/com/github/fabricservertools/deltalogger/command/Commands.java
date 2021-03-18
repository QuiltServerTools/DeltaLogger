package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.command.rollback.RollbackCommand;
import com.github.fabricservertools.deltalogger.command.search.SearchCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager.literal("dl").build();

		dispatcher.getRoot().addChild(deltaNode);

		InspectCommand.register(deltaNode);
		SearchCommand.register(deltaNode);
		ResetPassCommand.register(deltaNode);
		InfoCommand.register(deltaNode);
		RollbackCommand.register(deltaNode);
	}

}
