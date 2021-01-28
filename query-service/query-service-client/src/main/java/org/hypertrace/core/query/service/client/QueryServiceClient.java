package org.hypertrace.core.query.service.client;

import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.hypertrace.core.grpcutils.client.GrpcClientRequestContextUtil;
import org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.QueryServiceGrpc;
import org.hypertrace.core.query.service.api.QueryServiceGrpc.QueryServiceBlockingStub;
import org.hypertrace.core.query.service.api.ResultSetChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryServiceClient {
  private static final Logger LOG = LoggerFactory.getLogger(QueryServiceClient.class);
  /**
   * Since Pinot truncates the GroupBy results to 10, we need to set higher value when we need more
   * values than 10 or all results. We might need to increase it to even higher but starting with a
   * reasonably small value.
   */
  public static final int DEFAULT_QUERY_SERVICE_GROUP_BY_LIMIT = 10000;

  private final QueryServiceBlockingStub queryServiceClient;

  public QueryServiceClient(QueryServiceConfig queryServiceConfig) {
    ManagedChannel managedChannel =
        ManagedChannelBuilder.forAddress(
                queryServiceConfig.getQueryServiceHost(), queryServiceConfig.getQueryServicePort())
            .usePlaintext()
            .build();
    queryServiceClient =
        QueryServiceGrpc.newBlockingStub(managedChannel)
            .withCallCredentials(
                RequestContextClientCallCredsProviderFactory.getClientCallCredsProvider().get());
  }

  public Iterator<ResultSetChunk> executeQuery(
      QueryRequest request, Map<String, String> context, int timeoutMillis) {
    LOG.debug(
        "Sending query to query service with timeout: {}, and request: {}", timeoutMillis, request);
    return GrpcClientRequestContextUtil.executeWithHeadersContext(
        context,
        () ->
            queryServiceClient
                .withDeadline(Deadline.after(timeoutMillis, TimeUnit.MILLISECONDS))
                .execute(request));
  }
}
