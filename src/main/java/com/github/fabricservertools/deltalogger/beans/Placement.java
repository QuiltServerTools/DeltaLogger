package com.github.fabricservertools.deltalogger.beans;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.github.fabricservertools.deltalogger.Chat.format;
import static com.github.fabricservertools.deltalogger.Chat.joinText;

import static com.github.fabricservertools.deltalogger.command.rollback.RollbackUtils.createIdentifier;
import static com.github.fabricservertools.deltalogger.command.rollback.RollbackUtils.getBlock;

/**
 * POJO representing a block placement
 */
public class Placement implements Bean {
	private int id;
	private String playerName;
	private String time;
	private String blockType;
	private int x;
	private int y;
	private int z;
	private boolean placed;
	private String dimension;
	private String state;

	public Placement(int id, String playerName, String time, String blockType, String state, int x, int y, int z,
					 boolean placed, String dimension) {
		this.id = id;
		this.playerName = playerName;
		this.time = time;
		this.blockType = blockType;
		this.x = x;
		this.y = y;
		this.z = z;
		this.placed = placed;
		this.dimension = dimension;
		this.state = state;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
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

	public String getBlockType() {
		return this.blockType;
	}

	public void setBlockType(String blockType) {
		this.blockType = blockType;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return this.z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public boolean isPlaced() {
		return this.placed;
	}

	public boolean getPlaced() {
		return this.placed;
	}

	public void setPlaced(boolean placed) {
		this.placed = placed;
	}

	public String getDimension() {
		return this.dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	@Override
	public String toString() {
		return String.join(" ", time, playerName, placed ? "placed" : "removed", blockType);
	}

	public MutableText getText() {
		return joinText(format(time, Formatting.GRAY), format(playerName, Formatting.ITALIC),
				format(placed ? "placed" : "removed", placed ? Formatting.GREEN : Formatting.DARK_RED),
				format(blockType.replaceFirst("^minecraft:", ""), Formatting.YELLOW));
	}

	public MutableText getTextWithPos() {
		return joinText(format(time, Formatting.GRAY),
				format(new LiteralText(x + " " + y + " " + z), Formatting.AQUA),
				format(playerName, Formatting.ITALIC), getActionText(),
				getTranslatedBlock());
	}

	private MutableText getActionText() {
		MutableText text;
		if (placed) {
			text = new TranslatableText("placed").formatted(Formatting.GREEN);
		} else {
			text = new TranslatableText("removed").formatted(Formatting.RED);
		}
		return text;
	}

	private MutableText getTranslatedBlock() {
		blockType.replaceFirst("^minecraft:", "");
		return new TranslatableText(blockType).formatted(Formatting.YELLOW);
	}

	@Override
	public void rollback(World world) {
		//Every placement is called here, where the block setting logic is
		//Generates a BlockState object from identifier
		//BlockState state = getBlock(createIdentifier(blockType)).getDefaultState();
		CompoundTag allTag = new CompoundTag();
		allTag.putString("Name", createIdentifier(blockType).toString());
		try {
			allTag.put("Properties", StringNbtReader.parse(state));
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		BlockState state = NbtHelper.toBlockState(allTag);
		if (getPlaced()) {
			world.setBlockState(new BlockPos(getX(), getY(), getZ()), Blocks.AIR.getDefaultState());
		} else {
			world.setBlockState(new BlockPos(getX(), getY(), getZ()), state);
		}
	}
}