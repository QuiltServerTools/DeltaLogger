package com.github.fabricservertools.deltalogger.command;

import java.util.HashMap;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InspectCommand {
    boolean defaultDim;
    private static HashMap<PlayerEntity, Boolean> toolMap = new HashMap<>();

    public InspectCommand(boolean defaultDim) {
        this.defaultDim = defaultDim;
    }

    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager.literal("inspect")
                .executes(context -> toggleTool(context))
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .executes(context -> inspect(context, null, 10))
                        .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .then(CommandManager.argument("limit", IntegerArgumentType.integer())
                                        .executes(context -> inspect(context,
                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                IntegerArgumentType.getInteger(context, "limit"))))
                                .executes(context -> inspect(context,
                                        DimensionArgumentType.getDimensionArgument(context, "dimension"), 10)))
                        .then(CommandManager.argument("limit", IntegerArgumentType.integer())
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(context -> inspect(context, DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                IntegerArgumentType.getInteger(context, "limit"))))
                                .executes(context -> inspect(context, null,
                                        IntegerArgumentType.getInteger(context, "limit")))))
                .build();

        root.addChild(inspectNode);
    }

    private static int toggleTool(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity p;
        try {
            p = ctx.getSource().getPlayer();
            boolean mode = !toolMap.getOrDefault(p, false);
            toolMap.put(p, mode);
            p.sendMessage(new TranslatableText("DeltaLogger inspect mode ").append(new TranslatableText(mode ? "on" : "off").formatted(mode ? Formatting.GREEN : Formatting.RED)), true);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static boolean hasToolEnabled(PlayerEntity p) {
        return toolMap.getOrDefault(p, false);
    }

    private static int inspect(CommandContext<ServerCommandSource> scs, ServerWorld dim, int limit)
            throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(scs, "pos");
        World world = scs.getSource().getWorld();

        // Use player current dim otherwise if null arg
        Identifier dimension = dim == null ? world.getRegistryKey().getValue() : dim.getRegistryKey().getValue();

        MutableText placementMessage = DAO.block.getLatestPlacementsAt(dimension, pos, 0, limit).stream()
                .map(p -> p.getText()).reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                .map(txt -> Chat.concat("\n", new TranslatableText("Placement history"), txt))
                .orElse(new TranslatableText("No placements found with the terms specified").append(new LiteralText(pos.toString())));

        // FIXME: fix getting block from server console
        Chat.send(scs.getSource().getPlayer(), placementMessage);
        return 1;
    }
}
