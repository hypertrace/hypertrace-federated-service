package org.hypertrace.core.query.service;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import org.hypertrace.core.query.service.api.ResultSetChunk;
import org.hypertrace.core.query.service.api.ResultSetMetadata;
import org.hypertrace.core.query.service.api.Row;

/**
 * Batches received rows into chunks. The first chunk will contain result metadata, and the last
 * chunk will be marked with the `isLastChunk` flag. Always emits at least one chunk, unless
 * receiving an error, which is propagated as is.
 */
public class RowChunkingOperator implements ObservableOperator<ResultSetChunk, Row> {
  private static final int DEFAULT_CHUNK_ROWS = 10_000;

  public static RowChunkingOperator chunkRows(ResultSetMetadata resultSetMetadata) {
    return chunkRows(resultSetMetadata, DEFAULT_CHUNK_ROWS);
  }

  public static RowChunkingOperator chunkRows(ResultSetMetadata resultSetMetadata, int maxRows) {
    return new RowChunkingOperator(resultSetMetadata, maxRows);
  }

  private final int chunkRows;
  private final ResultSetChunk.Builder initialBuilder;

  private RowChunkingOperator(ResultSetMetadata resultSetMetadata, int chunkRows) {
    this.initialBuilder = ResultSetChunk.newBuilder().setResultSetMetadata(resultSetMetadata);
    this.chunkRows = chunkRows;
  }

  @Override
  public @NonNull Observer<? super Row> apply(@NonNull Observer<? super ResultSetChunk> observer) {
    return new ChunkingRowObserver(this.initialBuilder.clone(), this.chunkRows, observer);
  }

  static class ChunkingRowObserver extends DisposableObserver<Row> {
    private final int maxChunkRows;
    private final Observer<? super ResultSetChunk> downstream;
    private final ResultSetChunk.Builder currentBuilder;
    private int currentChunkRows;
    private int chunkId;
    private boolean done;

    ChunkingRowObserver(
        ResultSetChunk.Builder initialBuilder,
        int maxChunkRows,
        Observer<? super ResultSetChunk> chunkObserver) {
      this.currentBuilder = initialBuilder;
      this.maxChunkRows = maxChunkRows;
      this.downstream = chunkObserver;
    }

    @Override
    public void onStart() {
      this.downstream.onSubscribe(this);
    }

    @Override
    public void onNext(@NonNull Row row) {
      if (this.done) {
        return;
      }
      this.currentChunkRows++;
      if (this.currentChunkRows > this.maxChunkRows) {
        ResultSetChunk resultSetChunk = this.currentBuilder.build();
        this.downstream.onNext(resultSetChunk);
        this.currentBuilder.clear();
        this.chunkId++;
        this.currentChunkRows = 0;
        this.currentBuilder.setChunkId(this.chunkId);
      }

      this.currentBuilder.addRow(row);
    }

    @Override
    public void onError(@NonNull Throwable error) {
      if (this.done) {
        RxJavaPlugins.onError(error);
        return;
      }
      this.done = true;
      this.downstream.onError(error);
    }

    @Override
    public void onComplete() {
      if (this.done) {
        return;
      }
      this.done = true;
      this.currentBuilder.setIsLastChunk(true);
      ResultSetChunk resultSetChunk = this.currentBuilder.build();
      this.downstream.onNext(resultSetChunk);
      this.downstream.onComplete();
    }
  }
}
