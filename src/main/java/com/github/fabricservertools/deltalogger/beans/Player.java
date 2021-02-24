package com.github.fabricservertools.deltalogger.beans;

import java.util.UUID;

/**
 * POJO representing a player session
 */
public class Player {
	private int id;
	private UUID uuid;
	private String name;
	private String lastOnlineTime;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastOnlineTime() {
		return this.lastOnlineTime;
	}

	public void setLastOnlineTime(String lastOnlineTime) {
		this.lastOnlineTime = lastOnlineTime;
	}

	@Override
	public String toString() {
		return "{" +
				" id='" + getId() + "'" +
				", uuid='" + getUuid() + "'" +
				", name='" + getName() + "'" +
				", last_online_time='" + getLastOnlineTime() + "'" +
				"}";
	}


	public Player(int id, UUID uuid, String name, String lastOnlineTime) {
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.lastOnlineTime = lastOnlineTime;
	}
}