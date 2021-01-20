package com.github.fabricservertools.deltalogger.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RollbackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("rollback").then(CommandManager
                .argument("time", TimeArgumentType.time()).executes(ctx -> startRollback(ctx.getSource()))));
    }

    private static int startRollback(ServerCommandSource source) {
        return 1;
    }
}