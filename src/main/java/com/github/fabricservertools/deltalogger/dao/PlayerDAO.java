package com.github.fabricservertools.deltalogger.dao;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Player;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerDAO {
	private Jdbi jdbi;

	public PlayerDAO(Jdbi jdbi) {
		this.jdbi = jdbi;
		jdbi.registerRowMapper(Player.class, (rs, ctx) -> {
			return new Player(
					rs.getInt("id"),
					UUID.fromString(rs.getString("uuid")),
					rs.getString("name"),
					rs.getString("last_online_time")
			);
		});
	}

	public Optional<Player> getPlayerById(int id) {
		return jdbi.withHandle(handle -> handle
				.createQuery("SELECT * FROM players WHERE id = ?")
				.bind(0, id)
				.mapTo(Player.class)
				.findOne());
	}

	public Optional<Player> getPlayerByUUID(UUID uuid) {
		return jdbi.withHandle(handle -> handle
				.createQuery("SELECT * FROM players WHERE uuid = ?")
				.bind(0, uuid)
				.mapTo(Player.class)
				.findOne());
	}

	public List<Player> getPlayers(int offset, int limit) {
		return jdbi.withHandle(handle -> handle
				.select("SELECT * FROM players LIMIT ?,?", offset, limit)
				.mapTo(Player.class)
				.list());
	}

	public static QueueOperation insert(UUID uuid, String name, Instant lastOnline) {
		return new QueueOperation() {
			public int getPriority() {
				return 1;
			}

			public PreparedBatch prepareBatch(Handle handle) {
				return handle.prepareBatch(String.join(" ",
						"INSERT INTO players (uuid, name, last_online_time) VALUES (:id, :name, :last_online_time)",
						SQLUtils.onDuplicateKeyUpdate("uuid"), "uuid=:id, name=:name, last_online_time=:last_online_time"
				));
			}

			public PreparedBatch addBindings(PreparedBatch batch) {
				return batch
						.bind("id", uuid.toString())
						.bind("name", name)
						.bind("last_online_time", SQLUtils.instantToUTCString(lastOnline))
						.add();
			}
		};
	}
}
