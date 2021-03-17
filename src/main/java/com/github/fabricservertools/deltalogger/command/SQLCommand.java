package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.beans.Transaction;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class SQLCommand {
	public static void register(LiteralCommandNode<ServerCommandSource> root) {
		LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager.literal("sql")
				.requires(source -> source.hasPermissionLevel(4))
				.then(CommandManager.literal("block").then(CommandManager
						.argument("query", MessageArgumentType.message())
						.executes(context -> queryBlock(context.getSource(),
								MessageArgumentType.getMessage(context, "query")))))
				.then(CommandManager.literal("transaction").then(CommandManager
						.argument("query", MessageArgumentType.message())
						.executes(scs -> queryTransaction(scs.getSource(),
								MessageArgumentType.getMessage(scs, "query")))))
				.build();

		root.addChild(inspectNode);
	}

	private static int queryBlock(ServerCommandSource scs, Text sql) throws CommandSyntaxException {
		MutableText message = DAO.block.customQuery(sql.asString()).stream().map(Placement::getTextWithPos)
				.reduce((p1, p2) -> Chat.concat("\n", p1, p2))
				.map(txt -> Chat.concat("\n", Chat.text("Results for custom block query"), txt))
				.orElse(Chat.text("No results found with the terms specified"));
		scs.getPlayer().sendSystemMessage(message, Util.NIL_UUID);
		return 1;
	}

	private static int queryTransaction(ServerCommandSource scs, Text sql) throws CommandSyntaxException {
		MutableText message = DAO.transaction.customQuery(sql.asString()).stream().map(Transaction::getText)
				.reduce((p1, p2) -> Chat.concat("\n", p1, p2))
				.map(txt -> Chat.concat("\n", Chat.text("Results for custom transaction query"), txt))
				.orElse(Chat.text("No results found with the terms specified"));
		scs.getPlayer().sendSystemMessage(message, Util.NIL_UUID);
		return 1;
	}
}
