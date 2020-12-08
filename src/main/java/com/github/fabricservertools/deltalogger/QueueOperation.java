package com.github.fabricservertools.deltalogger;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;

/**
 * QUEUE Operation classes to insert into the priority queue
 */
public abstract class QueueOperation {
  public abstract PreparedBatch addBindings(PreparedBatch batch);
  public abstract PreparedBatch prepareBatch(Handle handle);
  public abstract int getPriority();

  protected PreparedBatch batch;

  public int[] execute(Handle handle) {
    if (this.batch == null) this.batch = this.prepareBatch(handle);
    return addBindings(batch).execute();
  }
}
