package com.github.fabricservertools.deltalogger.beans;

import static com.github.fabricservertools.deltalogger.Chat.*;

import java.util.UUID;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

/**
 * POJO representing an accounting of transaction interactions with inventory
 * screens.
 */
public class Transaction {
  private int id;
  private String playerName;
  private String time;
  private String itemType;
  private int count;
  private UUID containerUUID;

  public Transaction(int id, String playerName, String time, String itemType, int count, UUID containerUUID) {
    this.id = id;
    this.playerName = playerName;
    this.time = time;
    this.itemType = itemType;
    this.count = count;
    this.containerUUID = containerUUID;
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

  public MutableText getText() {
    return joinText(
      format(time, Formatting.GRAY),
      format(playerName, Formatting.ITALIC),
      format(
        count < 0 ? "took" : "put",
        count < 0 ? Formatting.DARK_RED : Formatting.GREEN
      ),
      format(Integer.valueOf(Math.abs(count))),
      format(itemType.replaceFirst("^minecraft:", ""), Formatting.YELLOW)
    );
  }
}
