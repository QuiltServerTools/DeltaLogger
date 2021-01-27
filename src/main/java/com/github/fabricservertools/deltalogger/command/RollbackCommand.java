package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.TimeParser;
import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RollbackCommand {
    private BlockPos[] positions = {};
    private ServerCommandSource scs;
    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> rollbackNode = literal("rollback").then(
                argument("radius", IntegerArgumentType.integer()).then(argument("time", StringArgumentType.string())
                        .then(argument("criteria", StringArgumentType.greedyString())
                                .suggests(RollbackParser.getInstance())
                                .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "criteria"),
                                        ctx.getSource().getPlayer(), IntegerArgumentType.getInteger(ctx, "radius"),
                                        StringArgumentType.getString(ctx, "time"))))))
                .build();
        root.addChild(rollbackNode);
    }

    private static int execute(ServerCommandSource source, String criteria, ServerPlayerEntity sourcePlayer, int radius,
            String timeString) {
        RollbackCommand rc = new RollbackCommand();
        rc.startRollback(source, criteria, sourcePlayer, radius, timeString);
        return 1;
    }

    private void startRollback(ServerCommandSource source, String criteria, ServerPlayerEntity sourcePlayer, int radius,
            String timeString) {
            scs = source;
        Duration time = TimeParser.parseTime(timeString);
        BlockPos playerPos = sourcePlayer.getBlockPos();
        int x1 = playerPos.getX() + radius;
        int y1 = playerPos.getY() + radius;
        int z1 = playerPos.getZ() + radius;
        int x2 = playerPos.getX() - radius;
        int y2 = playerPos.getY() - radius;
        int z2 = playerPos.getZ() - radius;
        int[] boxBounds = { x1, y1, z1, x2, y2, z2 };
        BlockBox box = new BlockBox(boxBounds);
        BlockPos.stream(box).forEach(this::addBlocks);
    }

    public void addBlocks(BlockPos pos) {
        // Rollback blocks
        ServerWorld dim = null;
        World world = scs.getWorld();
        Identifier dimension = dim == null ? world.getRegistryKey().getValue() : dim.getRegistryKey().getValue();
        
    }
}