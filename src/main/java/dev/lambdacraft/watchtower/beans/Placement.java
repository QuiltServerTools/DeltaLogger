package dev.lambdacraft.watchtower.beans;

import java.time.Instant;

import static dev.lambdacraft.watchtower.Chat.*;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * POJO representing a block placement
 */
public class Placement {
  private String playerName;
  private Instant time;
  private Identifier block;
  private BlockPos pos;
  private boolean placed;
  private Identifier dimension;

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public Identifier getBlock() {
    return block;
  }

  public void setBlock(Identifier block) {
    this.block = block;
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }

  public boolean isPlaced() {
    return placed;
  }

  public void setPlaced(boolean placed) {
    this.placed = placed;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  @Override
  public String toString() {
    return String.join(" ",
      time.toString(), playerName, placed ? "placed" : "removed", block.toString()
    );
  }

  public MutableText getText() {
    return joinText(
      format(time),
      format(playerName, Formatting.ITALIC),
      format(
        placed ? "placed" : "removed",
        placed ? Formatting.GREEN : Formatting.DARK_RED
      ),
      format(block)
    );
  }

  public Placement(String playerName, Instant time, Identifier block, BlockPos pos, boolean placed, Identifier dimension) {
    this.playerName = playerName;
    this.time = time;
    this.block = block;
    this.pos = pos;
    this.placed = placed;
  }
}