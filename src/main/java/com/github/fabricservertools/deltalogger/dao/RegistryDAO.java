package com.github.fabricservertools.deltalogger.dao;

import com.github.fabricservertools.deltalogger.QueueOperation;
import net.minecraft.util.Identifier;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

public class RegistryDAO {
	private Jdbi jdbi;

	public RegistryDAO(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	public static QueueOperation insert(Identifier id) {
		return new QueueOperation() {
			public int getPriority() {
				return 0;
			}

			public PreparedBatch prepareBatch(Handle handle) {
				return handle.prepareBatch("INSERT INTO registry (name) SELECT :name WHERE NOT EXISTS (SELECT 1 FROM registry WHERE name=:name)");
			}

			public PreparedBatch addBindings(PreparedBatch batch) {
				return batch.bind("name", id.toString()).add();
			}
		};
	}
}
