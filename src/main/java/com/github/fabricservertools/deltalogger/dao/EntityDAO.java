package com.github.fabricservertools.deltalogger.dao;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.KilledEntity;
import com.github.fabricservertools.deltalogger.beans.MobGrief;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EntityDAO {
	private Jdbi jdbi;

	public EntityDAO(Jdbi jdbi) {
		this.jdbi = jdbi;
		jdbi.registerRowMapper(KilledEntity.class, (rs, ctx) -> {
			return new KilledEntity(
					rs.getInt("id"),
					rs.getString("source"),
					rs.getString("killer"),
					rs.getString("dimension"),
					rs.getString("date"),
					rs.getInt("x"),
					rs.getInt("y"),
					rs.getInt("z"),
					rs.getString("entity_type"),
					rs.getString("entity_nbt")
			);
		});

		jdbi.registerRowMapper(MobGrief.class, (rs, ctx) -> new MobGrief(
				rs.getInt("id"),
				rs.getString("date"),
				rs.getString("entity"),
				rs.getString("target"),
				rs.getString("dimension"),
				rs.getInt("x"),
				rs.getInt("y"),
				rs.getInt("z")
		));
	}

	public List<MobGrief> searchGriefs(int idOffset, int limit, String where) {
		return jdbi.withHandle(handle -> handle
				.select(String.join(" ",
						"SELECT MG.id, date, ER.name as entity, PL.name as target, DR.name as dimension, x, y, z",
						"FROM (SELECT * FROM mob_grief WHERE id <",
						SQLUtils.offsetOrZeroLatest("mob_grief", "id", idOffset),
						where,
						"ORDER BY `id` DESC LIMIT ?) as MG",
						"LEFT JOIN players as PL ON MG.target = PL.id",
						"INNER JOIN registry as ER ON MG.entity_type = ER.id",
						"LEFT JOIN registry as DR ON MG.dimension_id = DR.id"
				), limit)
				.mapTo(MobGrief.class)
				.list()
		);
	}

	public List<KilledEntity> getLatestKilledEntities(int limit) {
		return jdbi.withHandle(handle -> handle
				.select(String.join(" ",
						"SELECT KE.id, KE.entity_nbt, source, PL.name as killer, DR.name as dimension, date, x, y, z",
						"FROM (SELECT * FROM killed_entities",
						"ORDER BY `id` DESC LIMIT ?) as KE",
						"LEFT JOIN players as PL ON KE.killer_id = PL.id",
						"LEFT JOIN registry as DR ON KE.dimension_id = DR.id"
				), limit)
				.mapTo(KilledEntity.class)
				.list()
		);
	}

	public List<KilledEntity> searchEntities(String where) {
		return jdbi.withHandle(handle -> handle
				.select(String.join(" ",
						"SELECT KE.id, KE.entity_nbt, source, PL.name as killer, DR.name as dimension, date, x, y, z",
						"FROM (SELECT * FROM killed_entities WHERE ",
						where,
						"ORDER BY `id` DESC LIMIT ?) as KE",
						"LEFT JOIN players as PL ON KE.killer_id = PL.id",
						"LEFT JOIN registry as DR ON KE.dimension_id = DR.id"
				))
				.mapTo(KilledEntity.class)
				.list()
		);
	}

	public List<KilledEntity> rollbackEntities(String where, String time, Identifier dimension, BlockPos posS, BlockPos posL) {
		return jdbi.withHandle(handle -> handle
				.select(String.join(" ",
						"SELECT KE.id, DE.name as entity_type, KE.entity_nbt, source, PL.name as killer, DR.name as dimension, date, x, y, z",
						"FROM (SELECT * FROM killed_entities WHERE",
						"x >= :xs AND x <= :xl AND y >= :ys AND y <= :yl AND z >= :zs AND z <= :zl AND dimension_id = (SELECT id FROM registry WHERE name = :dim) AND date > \""+time+"\"",
						where,
						"ORDER BY `id` DESC) as KE",
						"LEFT JOIN players as PL ON KE.killer_id = PL.id",
						"LEFT JOIN registry as DR ON KE.dimension_id = DR.id",
						"LEFT JOIN registry as DE ON KE.entity_type = DE.id"
				))
				.bind("dim", dimension.toString())
				.bind("xs", posS.getX()).bind("xl", posL.getX()).bind("ys", posS.getY()).bind("yl", posL.getY()).bind("zs", posS.getZ()).bind("zl", posL.getZ())
				.mapTo(KilledEntity.class)
				.list()
		);
	}

	public static QueueOperation insertMobGrief(
			UUID target,
			Instant date,
			Identifier entityType,
			BlockPos pos,
			Identifier dimensionId
	) {
		return new QueueOperation() {

			public PreparedBatch prepareBatch(Handle handle) {
				return handle.prepareBatch(String.join("",
						"INSERT INTO mob_grief (date, entity_type, target, x, y, z, dimension_id) ",
						"SELECT :date, registry.id, players.id, :x, :y, :z,",
						"(SELECT id FROM registry WHERE registry.name=:dimension)",
						"FROM players, registry ",
						"WHERE players.uuid=:target AND registry.name=:entityType "
				));
			}

			public PreparedBatch addBindings(PreparedBatch batch) {
				return batch
						.bind("date", SQLUtils.instantToUTCString(date))
						.bind("target", target.toString())
						.bind("entityType", entityType.toString())
						.bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
						.bind("dimension", dimensionId.toString())
						.add();
			}

			public int getPriority() {
				return 2;
			}
		};
	}

	public static QueueOperation insertKill(
			String sourceName,
			UUID killer_id,
			Instant date,
			BlockPos pos,
			Identifier dimId,
			Identifier entityType,
			CompoundTag tag
	) {
		return new QueueOperation() {
			public int getPriority() {
				return 2;
			}

			public PreparedBatch prepareBatch(Handle handle) {
				return handle.prepareBatch(String.join("",
						"INSERT INTO killed_entities (source, killer_id, date, x, y, z, dimension_id, entity_type, entity_nbt) ",
						"VALUES (:source, ",
						"(CASE WHEN :killer_id IS NULL THEN NULL ELSE (SELECT id FROM players WHERE uuid=:killer_id) END), ",
						":date, :x, :y, :z, (SELECT `id` FROM registry WHERE `name`=:dimension), (SELECT `id` FROM registry WHERE `name`=:entity), :nbt)"
				));
			}

			public PreparedBatch addBindings(PreparedBatch batch) {
				return batch
						.bind("source", sourceName)
						.bind("killer_id", killer_id != null ? killer_id.toString() : null)
						.bind("date", SQLUtils.instantToUTCString(date))
						.bind("x", pos.getX()).bind("z", pos.getZ()).bind("y", pos.getY())
						.bind("dimension", dimId.toString())
						.bind("entity", entityType.toString())
						.bind("nbt", tag != null ? tag.asString() : null)
						.add();
			}
		};
	}
}
