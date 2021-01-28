package org.hypertrace.core.query.service;

import static org.hypertrace.core.query.service.RowChunkingOperator.chunkRows;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import org.hypertrace.core.query.service.api.ResultSetChunk;
import org.hypertrace.core.query.service.api.ResultSetMetadata;
import org.hypertrace.core.query.service.api.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RowChunkingOperatorTest {
  private final ResultSetMetadata resultSetMetadata = ResultSetMetadata.newBuilder().build();

  private TestObserver<ResultSetChunk> testObserver;

  @BeforeEach
  void beforeEach() {
    this.testObserver = new TestObserver<>();
  }

  @Test
  void supportsEmptyObservable() {
    Observable.<Row>empty().lift(chunkRows(resultSetMetadata)).blockingSubscribe(this.testObserver);
    testObserver.assertResult(
        ResultSetChunk.newBuilder()
            .setChunkId(0)
            .setIsLastChunk(true)
            .setResultSetMetadata(this.resultSetMetadata)
            .build());
  }

  @Test
  void supportsObservableOfLessThanOneChunk() {
    Row onlyRow = Row.getDefaultInstance();
    Observable.just(onlyRow)
        .lift(chunkRows(resultSetMetadata))
        .blockingSubscribe(this.testObserver);
    testObserver.assertResult(
        ResultSetChunk.newBuilder()
            .setChunkId(0)
            .setIsLastChunk(true)
            .setResultSetMetadata(this.resultSetMetadata)
            .addRow(onlyRow)
            .build());
  }

  @Test
  void supportsObservableOfExactlyOneChunk() {
    Row row1 = Row.getDefaultInstance();
    Row row2 = Row.getDefaultInstance();
    Observable.just(row1, row2)
        .lift(chunkRows(resultSetMetadata, 2))
        .blockingSubscribe(this.testObserver);
    testObserver.assertResult(
        ResultSetChunk.newBuilder()
            .setChunkId(0)
            .setIsLastChunk(true)
            .setResultSetMetadata(this.resultSetMetadata)
            .addRow(row1)
            .addRow(row2)
            .build());
  }

  @Test
  void supportsObservableWithMultipleChunks() {
    Row row1 = Row.getDefaultInstance();
    Row row2 = Row.getDefaultInstance();
    Row row3 = Row.getDefaultInstance();
    Observable.just(row1, row2, row3)
        .lift(chunkRows(resultSetMetadata, 2))
        .blockingSubscribe(this.testObserver);
    testObserver.assertResult(
        ResultSetChunk.newBuilder()
            .setChunkId(0)
            .setResultSetMetadata(this.resultSetMetadata)
            .addRow(row1)
            .addRow(row2)
            .build(),
        ResultSetChunk.newBuilder().setChunkId(1).setIsLastChunk(true).addRow(row3).build());
  }

  @Test
  void propagatesError() {
    Observable.<Row>error(UnsupportedOperationException::new)
        .lift(chunkRows(resultSetMetadata))
        .blockingSubscribe(this.testObserver);
    this.testObserver.assertError(UnsupportedOperationException.class).assertNoValues();
  }
}
