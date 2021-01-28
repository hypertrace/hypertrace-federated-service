package org.hypertrace.graphql.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hypertrace.core.graphql.spi.lifecycle.GraphQlServiceLifecycle;

class DefaultGraphQlServiceLifecycle implements GraphQlServiceLifecycle {
  private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

  @Override
  public CompletionStage<Void> shutdownCompletion() {
    return this.completableFuture.minimalCompletionStage();
  }

  void shutdown() {
    this.completableFuture.complete(null);
  }
}
