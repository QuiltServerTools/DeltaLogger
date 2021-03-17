package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.vavr.control.Either;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Date;

public class ResetPassCommand {
	private static long resetEpoch = new Date().getTime();
	private static final long interval = 60000; //1 minute duration

	public static void register(LiteralCommandNode<ServerCommandSource> root) {
		LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager
				.literal("resetpass")
				.executes(context -> {
					ServerPlayerEntity p = context.getSource().getPlayer();
					Date now = new Date();
					if (now.getTime() > resetEpoch) {
						resetEpoch = now.getTime() + interval;
						if (p == null) {
							context.getSource().sendFeedback(new TranslatableText("deltalogger.reset_pass.error"), false);
							return 1;
						}
						Either<String, String> tempPass = DAO.auth.issueTemporaryPass(p.getUuid(), p.hasPermissionLevel(4));

						if (tempPass.isLeft()) {
							p.sendMessage(new LiteralText(tempPass.getLeft()), false);
						} else {
							p.sendMessage(new TranslatableText("deltalogger.temp_pass", tempPass.get()).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tempPass.get()))), false);
						}
					} else {
						p.sendMessage(new TranslatableText("deltalogger.reset_pass.wait", String.valueOf((resetEpoch - now.getTime()) / 1000)).formatted(Formatting.RED), false);
					}

					return 1;
				})
				.build();

		root.addChild(inspectNode);
	}
}
