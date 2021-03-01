package com.github.fabricservertools.deltalogger.dao;

import com.github.fabricservertools.deltalogger.QueueOperation;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Transaction;
import com.github.fabricservertools.deltalogger.beans.TransactionPos;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {
	private Jdbi jdbi;
	private final String SELECT_TRANSACTIONS = String.join(" ",
			"SELECT CT.id, C.uuid,", SQLUtils.getDateFormatted("CT.date", "date"),
			", R.name as `item_type`, CT.item_count, P.name as `player_name`"
	);
	private final String JOIN_TRANSACTIONS = String.join(" ",
			"INNER JOIN registry as R ON CT.item_type = R.id",
			"INNER JOIN containers as C ON CT.container_id = C.id",
			"LEFT JOIN registry as DT ON DT.id = C.dimension_id",
			"INNER JOIN players as P ON CT.player_id = P.id"
	);

	public TransactionDAO(Jdbi jdbi) {
		this.jdbi = jdbi;
		jdbi.registerRowMapper(Transaction.class,
				(rs, ctx) -> new Transaction(rs.getInt("id"), rs.getString("player_name"), rs.getString("date"),
						rs.getString("item_type"), rs.getInt("item_count"), UUID.fromString(rs.getString("uuid"))));

		jdbi.registerRowMapper(TransactionPos.class,
				(rs, ctx) -> new TransactionPos(rs.getInt("id"), rs.getString("player_name"), rs.getString("date"),
						rs.getString("item_type"), rs.getInt("item_count"), UUID.fromString(rs.getString("uuid")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
	}

	/**
	 * Get latest transactions
	 *
	 * @param idOffset must be the id of the row to offset from, if offset is 0 then get latest
	 * @param limit    the number of rows to return
	 * @return
	 */
	public List<Transaction> getLatestTransactions(int idOffset, int limit) {
		try {
			List<Transaction> tr = jdbi.withHandle(handle -> handle
					.select(String.join(" ",
							SELECT_TRANSACTIONS,
							"FROM (SELECT * FROM container_transactions WHERE id <",
							SQLUtils.offsetOrZeroLatest("container_transactions", "id", idOffset), // sql perf optim.
							"ORDER BY `id` DESC LIMIT ?) as CT",
							JOIN_TRANSACTIONS
					), limit)
					.mapTo(Transaction.class)
					.list()
			);

			return tr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<Transaction> getTransactionsAt(Identifier dimension, BlockPos pos, int limit) {
		try {
			return jdbi.withHandle(handle -> handle.select(
					String.join(" ",
							// "SELECT CT.id, C.uuid,", SQLUtils.getDateFormatted("CT.date", "date"), ", CT.item_count, P.name as `player_name`",
							SELECT_TRANSACTIONS,
							"FROM container_transactions as CT",
							JOIN_TRANSACTIONS,
							"WHERE C.x=? AND C.y=? AND C.z=? AND DT.name = ?",
							"ORDER BY CT.date DESC LIMIT ?"
					),
					pos.getX(), pos.getY(), pos.getZ(), dimension.toString(), limit
			).mapTo(Transaction.class).list());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<Transaction> search(int limit, String builtWhere) {
		try {
			return jdbi
					.withHandle(handle -> handle.select(String.join(" ",
							String.join(" ",
									"SELECT CT.id, C.x, C.y, C.z , C.uuid,", SQLUtils.getDateFormatted("CT.date", "date"),
									", R.name as `item_type`, CT.item_count, P.name as `player_name`"
							),
							"FROM container_transactions as CT",
							JOIN_TRANSACTIONS, builtWhere, "ORDER BY CT.date DESC LIMIT " + limit)
					).mapTo(Transaction.class).list());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<TransactionPos> rollbackQuery(Identifier dimension, BlockPos posS, BlockPos posL, String time, String criteria) {
		try {
			return jdbi.withHandle(handle -> handle.select(
					String.join(" ",
							// "SELECT CT.id, C.uuid,", SQLUtils.getDateFormatted("CT.date", "date"), ", CT.item_count, P.name as `player_name`",
							"SELECT CT.id, C.uuid,", SQLUtils.getDateFormatted("CT.date", "date"),
							", R.name as `item_type`, CT.item_count, P.name as `player_name`, C.x, C.y, C.z",
							"FROM container_transactions as CT",
							JOIN_TRANSACTIONS,
							"WHERE C.x >= :xs AND C.x <= :xl AND C.y >= :ys AND C.y <= :yl AND C.z >= :zs AND C.z <= :zl AND DT.name = :dim AND CT.date > :time",
							"ORDER BY CT.date"
					)
					//pos.getX(), pos.getY(), pos.getZ(), dimension.toString(), time
			).bind("xs", posS.getX()).bind("xl", posL.getX()).bind("ys", posS.getY()).bind("yl", posL.getY()).bind("zs", posS.getZ()).bind("zl", posL.getZ())
					.bind("dim", dimension.toString()).bind("time", time)

					.mapTo(TransactionPos.class).list());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<Transaction> customQuery(String sql) {
		try {
			return jdbi.withHandle(handle -> handle.select(sql, 100)).mapTo(Transaction.class).list();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public List<Transaction> getTransactionsFromUUID(UUID uuid, int limit) {
		try {
			return jdbi.withHandle(handle -> handle
					.select(String.join(" ",
							SELECT_TRANSACTIONS,
							"FROM container_transactions as CT",
							JOIN_TRANSACTIONS,
							"WHERE C.uuid=?",
							"ORDER BY CT.date DESC LIMIT ?"
					), uuid.toString(), limit)
					.mapTo(Transaction.class).list());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public static QueueOperation insert(UUID playerId, UUID containerId, Instant date, Identifier item_type,
										Integer item_count, byte[] item_data) {
		return new QueueOperation() {
			public int getPriority() {
				return 3;
			}

			public PreparedBatch prepareBatch(Handle handle) {
				return handle.prepareBatch(String.join("",
						"INSERT INTO container_transactions (player_id, container_id, date, item_type, item_count, item_data)",
						"SELECT players.id, containers.id, :date, registry.id, :item_count, :item_data ",
						"FROM players, containers, registry ",
						"WHERE players.uuid=:playerId AND containers.uuid=:containerId AND registry.name=:item_type "));
			}

			public PreparedBatch addBindings(PreparedBatch batch) {
				return batch.bind("playerId", playerId.toString()).bind("containerId", containerId.toString())
						.bind("date", SQLUtils.instantToUTCString(date)).bind("item_type", item_type.toString())
						.bind("item_count", item_count).bind("item_data", item_data).add();
			}
		};
	}
}
