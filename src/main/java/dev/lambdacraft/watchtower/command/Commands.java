package dev.lambdacraft.watchtower.command;

import java.util.HashMap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
        private static HashMap<PlayerEntity, Boolean> toolMap = new HashMap<>();
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
                LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager
                        .literal("wt")
                        .requires(source -> source.hasPermissionLevel(3))
                        .build();

                dispatcher.getRoot().addChild(deltaNode);

                InspectCommand.register(deltaNode);
                /*SearchCommand.register(deltaNode);
                */
                SQLCommand.register(deltaNode);
        }
        public static boolean hasToolEnabled(PlayerEntity p) {
                return toolMap.getOrDefault(p, false);
        }
}
