package com.github.fabricservertools.deltalogger.command;

import java.util.Calendar;
import java.util.Date;

import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.vavr.control.Either;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public final class Commands {
    private static long resetEpoch = new Date().getTime();
    private static final long interval = 60000; //1 minute duration
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> deltaNode = CommandManager.literal("dl")
                .requires(source -> source.hasPermissionLevel(3)).build();

        dispatcher.getRoot().addChild(deltaNode);

        InspectCommand.register(deltaNode);
        SearchCommand.register(deltaNode);
        SQLCommand.register(deltaNode);
        registerResetPass(deltaNode);
    }
    public static void registerResetPass(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager
            .literal("resetpass")
            .requires(source -> source.hasPermissionLevel(3))
            .executes(context -> {
                ServerPlayerEntity p = context.getSource().getPlayer();
                Date now = new Date();
                if (now.getTime()>resetEpoch) {
                    resetEpoch = now.getTime()+interval;
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
                }
                else {
                    p.sendMessage(new LiteralText("Please wait for "+/*Calendar.getInstance().get(Calendar.SECOND)*/(resetEpoch - now.getTime())/1000+" seconds before resetting your password").formatted(Formatting.RED),false);
                }
                
                return 1;
            })
            .build();

        root.addChild(inspectNode);
    }
}
