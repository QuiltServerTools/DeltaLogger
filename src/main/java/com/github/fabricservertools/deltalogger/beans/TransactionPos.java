package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

import static com.github.fabricservertools.deltalogger.Chat.format;
import static com.github.fabricservertools.deltalogger.Chat.joinText;
import static com.github.fabricservertools.deltalogger.command.rollback.RollbackUtils.createIdentifier;
import static com.github.fabricservertools.deltalogger.command.rollback.RollbackUtils.getItem;

/**
 * POJO representing an accounting of transaction interactions with inventory
 * screens.
 */
public class TransactionPos implements Bean {
	private int id;
	private String playerName;
	private String time;
	private String itemType;
	private int count;
	private UUID containerUUID;
	private final BlockPos pos;

	public TransactionPos(int id, String playerName, String time, String itemType, int count, UUID containerUUID, int x, int y, int z) {
		this.id = id;
		this.playerName = playerName;
		this.time = time;
		this.itemType = itemType;
		this.count = count;
		this.containerUUID = containerUUID;
		this.pos = new BlockPos(x, y, z);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getItemType() {
		return this.itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public UUID getContainerUUID() {
		return this.containerUUID;
	}

	public void setContainerUUID(UUID containerUUID) {
		this.containerUUID = containerUUID;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public MutableText getText() {
		return joinText(
				format(time, Formatting.GRAY),
				format(new LiteralText(pos.getX() + " " + pos.getY() + " " + pos.getZ()), Formatting.AQUA),
				format(playerName, Formatting.ITALIC),
				format(
						count < 0 ? "took" : "put",
						count < 0 ? Formatting.DARK_RED : Formatting.GREEN
				),
				format(Math.abs(count)),
				format(itemType.replaceFirst("^minecraft:", ""), Formatting.YELLOW)
		);
	}

	@Override
	public void rollback(World world) {
		BlockPos pos = getPos();
		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Inventory inventory;
		inventory = (Inventory) blockEntity;
		if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
			inventory = ChestBlock.getInventory((ChestBlock) block, state, world, pos, true);
		}

		int amount = getCount();
		ItemStack itemStack = new ItemStack(getItem(createIdentifier(getItemType())), amount * (amount < 0 ? -1 : 1));

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
					if (inventory.getStack(i).getItem().equals(getItem(createIdentifier(getItemType())))) {
						inventory.setStack(i, ItemStack.EMPTY);
						return;
					}
				}
			}
		}
	}
}
