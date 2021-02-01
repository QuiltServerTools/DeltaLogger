package com.github.fabricservertools.deltalogger.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;

public class InfoCommand {
    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> searchNode = literal("info")
                .then(literal("version").executes(context -> getVersion(context.getSource())))
                .then(literal("discord").executes(context -> getDiscord(context.getSource()))).build();

        root.addChild(searchNode);
    }

    private static int getVersion(ServerCommandSource scs) throws CommandSyntaxException {
        Optional<ModContainer> mc = FabricLoader.getInstance().getModContainer("deltalogger");
        if (mc.isPresent()) {
            ModContainer deltaLogger = mc.get();
            ModMetadata data = deltaLogger.getMetadata();
            Version version = data.getVersion();
            scs.getPlayer().sendSystemMessage(new LiteralText("You are using DeltaLogger v")
                    .append(version.getFriendlyString()).formatted(Formatting.GREEN), Util.NIL_UUID);
        }
        return 1;
    }

    private static int getDiscord(ServerCommandSource scs) throws CommandSyntaxException {
        scs.getPlayer().sendSystemMessage(new LiteralText("Click to join the DeltaLogger discord").styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/UxHnDWr"))).formatted(Formatting.GREEN), Util.NIL_UUID);
        return 1;
    }
}
