package dev.lambdacraft.watchtower.beans;

import java.time.Instant;
import java.util.UUID;

import static dev.lambdacraft.watchtower.Chat.*;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * POJO representing an accounting of transaction interactions with inventory
 * screens.
 */
public class Transaction {
  private Instant time;
  private String playerName;
  private Identifier item;
  private int count;
  private UUID containerUUID;

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public Identifier getItem() {
    return item;
  }

  public void setItem(Identifier item) {
    this.item = item;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public UUID getContainerUUID() {
    return containerUUID;
  }

  public void setContainerUUID(UUID containerUUID) {
    this.containerUUID = containerUUID;
  }

  public Transaction(Instant time, String playerName, Identifier item, int count, UUID containerUUID) {
    this.time = time;
    this.playerName = playerName;
    this.item = item;
    this.count = count;
    this.containerUUID = containerUUID;
  }

  public MutableText getText() {
    return joinText(
      format(time),
      format(playerName, Formatting.ITALIC),
      format(
        count < 0 ? "took" : "put",
        count < 0 ? Formatting.DARK_RED : Formatting.GREEN
      ),
      format(Integer.valueOf(Math.abs(count))),
      format(item)
    );
  }
}
