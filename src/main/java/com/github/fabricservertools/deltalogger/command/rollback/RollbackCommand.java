package com.github.fabricservertools.deltalogger.command.rollback;

import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.TimeParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RollbackCommand {
	public static void register(LiteralCommandNode<ServerCommandSource> root) {
		LiteralCommandNode<ServerCommandSource> rollbackNode = literal("rollback").then(
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
		String parsedCriteria = RollbackParser.criteria(criteria, sourcePlayer, source);

		int x1 = playerPos.getX() + radius;
		int y1 = playerPos.getY() + radius;
		int z1 = playerPos.getZ() + radius;
		int x2 = playerPos.getX() - radius;
		int y2 = playerPos.getY() - radius;
		int z2 = playerPos.getZ() - radius;
		
		World world = source.getWorld();
		Identifier dimension = world.getRegistryKey().getValue();
		BlockBox box = new BlockBox(boxBounds);

		rollbackBlocks(criteria, new BlockPos(x2, y2, z2), new BlockPos(x1, y1, z1), timeValue, dimension, world);

		source.sendFeedback(new TranslatableText("deltalogger.rollback.block.complete").formatted(Formatting.ITALIC, Formatting.GRAY).append(new TranslatableText("deltalogger.rollback.progress", 1, 2).formatted(Formatting.YELLOW)), false);

		rollbackTransactions(criteria, new BlockPos(x2, y2, z2), new BlockPos(x1, y1, z1), timeValue, dimension, world);

		source.sendFeedback(new TranslatableText("deltalogger.rollback.transaction.complete").formatted(Formatting.ITALIC, Formatting.GRAY).append(new TranslatableText("deltalogger.rollback.progress", 2, 2).formatted(Formatting.YELLOW)), false);

		sendFinishFeedback(source, timeValue);
	}

	private static void rollbackBlocks(String criteria, BlockPos posS, BlockPos posL, String time, Identifier dimension, World world) {
		// Rollback blocks
		DAO.block.rollbackQuery(dimension, posS, posL, time, criteria).forEach(placement -> {
			//Every placement is called here, where the block setting logic is
			//Generates a BlockState object from identifier
			BlockState state = getBlock(createIdentifier(placement.getBlockType())).getDefaultState();

			if (placement.getPlaced()) {
				world.setBlockState(new BlockPos(placement.getX(), placement.getY(), placement.getZ()), Blocks.AIR.getDefaultState());
			} else {
				world.setBlockState(new BlockPos(placement.getX(), placement.getY(), placement.getZ()), state);
			}

		});
	}

	private static void rollbackTransactions(String criteria, BlockPos posS, BlockPos posL, String time, Identifier dimension, World world) {



		DAO.transaction.rollbackQuery(dimension, posS, posL, time, criteria).forEach(transaction -> {
			BlockPos pos = transaction.getPos();
			BlockEntity blockEntity = world.getBlockEntity(pos);
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			Inventory inventory;
			inventory = (Inventory) blockEntity;
			if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
				inventory = ChestBlock.getInventory((ChestBlock) block, state, world, pos, true);
			}

			int amount = transaction.getCount();
			ItemStack itemStack = new ItemStack(getItem(createIdentifier(transaction.getItemType())), amount * (amount < 0 ? -1 : 1));

			if (inventory != null) {
				for (int i = 0; i < inventory.size(); i++) {
					if (amount < 0) {
						// Item was removed, add back
						if (inventory.getStack(i).isEmpty()) {
							inventory.setStack(i, itemStack);
							return;
						}
					} else {
						//Item was added, remove
						if (inventory.getStack(i).getItem().equals(getItem(createIdentifier(transaction.getItemType())))) {
							inventory.setStack(i, ItemStack.EMPTY);
							return;
						}
					}
				}
			}

		});
	}

	private static void sendFinishFeedback(ServerCommandSource scs, String time) {
		scs.sendFeedback(new TranslatableText("deltalogger.rollback.complete").formatted(Formatting.GREEN).append(new TranslatableText("deltalogger.rollback.progress", 2, 2).formatted(Formatting.YELLOW)), true);
	}

	private static Identifier createIdentifier(String identifier) {
		String[] identifierSplit = identifier.split(":");
		return new Identifier(identifierSplit[0], identifierSplit[1]);
	}

	private static Item getItem(Identifier id) {
		return Registry.ITEM.get(id);
	}

	private static Block getBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}
}