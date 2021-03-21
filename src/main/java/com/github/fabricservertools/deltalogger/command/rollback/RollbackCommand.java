package com.github.fabricservertools.deltalogger.command.rollback;

import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.command.DlPermissions;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.TimeParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RollbackCommand {
    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> rollbackNode = literal("rollback")
				.requires(scs -> DlPermissions.checkPerms(scs, "deltalogger.rollback"))
				.then(
                argument("radius", IntegerArgumentType.integer()).then(argument("time", StringArgumentType.string())
                        .executes(ctx -> execute(ctx.getSource(), "", ctx.getSource().getPlayer(),
                                IntegerArgumentType.getInteger(ctx, "radius"),
                                StringArgumentType.getString(ctx, "time")))
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
        RollbackCommand.startRollback(source, criteria, sourcePlayer, radius, timeString);
        return 1;
    }

    private static void startRollback(ServerCommandSource source, String criteria, ServerPlayerEntity sourcePlayer,
                                      int radius, String timeString) {
        Duration duration = TimeParser.parseTime(timeString);
        String timeValue = SQLUtils.instantToUTCString(Instant.now().minus(duration.getSeconds(), ChronoUnit.SECONDS));
        BlockPos playerPos = sourcePlayer.getBlockPos();

        String parsedCriteria;

        try {
            parsedCriteria = RollbackParser.criteria(criteria, source);
        } catch (
                CommandSyntaxException e) {
            sourcePlayer.sendSystemMessage(new LiteralText("Error parsing criteria").formatted(Formatting.RED), Util.NIL_UUID);
            return;
        }

        int x1 = playerPos.getX() + radius;
        int y1 = playerPos.getY() + radius;
        int z1 = playerPos.getZ() + radius;
        int x2 = playerPos.getX() - radius;
        int y2 = playerPos.getY() - radius;
        int z2 = playerPos.getZ() - radius;

        World world = source.getWorld();
        Identifier dimension = world.getRegistryKey().getValue();

        rollbackBlocks(parsedCriteria, new BlockPos(x2, y2, z2), new BlockPos(x1, y1, z1), timeValue, dimension, world);

        source.sendFeedback(new TranslatableText("deltalogger.rollback.block.complete").formatted(Formatting.ITALIC, Formatting.GRAY).append(new TranslatableText("deltalogger.rollback.progress", 1, 2).formatted(Formatting.YELLOW)), false);

        rollbackTransactions(parsedCriteria, new BlockPos(x2, y2, z2), new BlockPos(x1, y1, z1), timeValue, dimension, world);

        source.sendFeedback(new TranslatableText("deltalogger.rollback.transaction.complete").formatted(Formatting.ITALIC, Formatting.GRAY).append(new TranslatableText("deltalogger.rollback.progress", 2, 2).formatted(Formatting.YELLOW)), false);

        sendFinishFeedback(source);
    }

    private static void rollbackBlocks(String criteria, BlockPos posS, BlockPos posL, String time, Identifier dimension, World world) {
        // Rollback blocks
        DAO.block.rollbackQuery(dimension, posS, posL, time, criteria).forEach(placement -> placement.rollback(world));
    }

    private static void rollbackTransactions(String criteria, BlockPos posS, BlockPos posL, String time, Identifier dimension, World world) {
        DAO.transaction.rollbackQuery(dimension, posS, posL, time, criteria).forEach(transaction -> transaction.rollback(world));
    }

    private static void sendFinishFeedback(ServerCommandSource scs) {
        scs.sendFeedback(new TranslatableText("deltalogger.rollback.complete").formatted(Formatting.GREEN).append(new TranslatableText("deltalogger.rollback.progress", 2, 2).formatted(Formatting.YELLOW)), true);
    }
}