package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.vavr.control.Either;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public final class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager.literal("dl")
                .requires(source -> source.hasPermissionLevel(3)).build();

        dispatcher.getRoot().addChild(deltaNode);

        InspectCommand.register(deltaNode);
        SearchCommand.register(deltaNode);
        SQLCommand.register(deltaNode);
        RollbackCommand.register(deltaNode);
        registerResetPass(deltaNode);
    }

    // FIXME: put rate limiter on command because issuing new pass takes lots of compute
    public static void registerResetPass(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager
            .literal("resetpass")
            // .requires(source -> source.hasPermissionLevel(level))
            .executes(context -> {
                ServerPlayerEntity p = context.getSource().getPlayer();
                if (p == null) {
                    context.getSource().sendFeedback(new LiteralText("Command resetpass error: must be logged in as user to reset password."), false);
                    return 1;
                }
                Either<String, String> tempPass = DAO.auth.issueTemporaryPass(p.getUuid(), p.hasPermissionLevel(4));
                
                if (tempPass.isLeft()) {
                    p.sendMessage(new LiteralText(tempPass.getLeft()), false);
                } else {
                    p.sendMessage(new LiteralText("Your temporary password for DeltaLogger panel is " + tempPass.get()), false);
                }
                return 1;
            })
            .build();

        root.addChild(inspectNode);
    }
}
