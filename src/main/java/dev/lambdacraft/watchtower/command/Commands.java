package dev.lambdacraft.watchtower.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager.literal("dl")
                .requires(source -> source.hasPermissionLevel(3)).build();

        dispatcher.getRoot().addChild(deltaNode);

        InspectCommand.register(deltaNode);
        /*
         * SearchCommand.register(deltaNode);
         */
        SQLCommand.register(deltaNode);
    }
}
