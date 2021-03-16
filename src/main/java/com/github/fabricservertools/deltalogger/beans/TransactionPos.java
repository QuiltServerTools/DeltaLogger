package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static com.github.fabricservertools.deltalogger.Chat.format;
import static com.github.fabricservertools.deltalogger.Chat.joinText;

/**
 * POJO representing an accounting of transaction interactions with inventory
 * screens.
 */
public class TransactionPos {
	private int id;
	private String playerName;
	private String time;
	private String itemType;
	private int count;
	private UUID containerUUID;
	private BlockPos pos;

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
}
