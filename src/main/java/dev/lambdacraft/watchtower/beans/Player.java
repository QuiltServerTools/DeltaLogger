package dev.lambdacraft.watchtower.beans;

import java.util.UUID;

/**
 * POJO representing a player session
 */
public class Player {
  private UUID uuid;
  private String name;
  private String lastonline;

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastOnline() {
    return lastonline;
  }

  public void setLastlogin(String lastonline) {
    this.lastonline = lastonline;
  }

  @Override
  public String toString() {
    return "Player [lastonline=" + lastonline + ", name=" + name + ", uuid=" + uuid + "]";
  }

  public Player(UUID uuid, String name, String lastonline) {
    this.uuid = uuid;
    this.name = name;
    this.lastonline = lastonline;
  }
}