# Query Service

Run it with:

../gradlew run

Test the client using integration test (this requires E2E platform setup with sample data):

sh run-integration-tests.sh

Sample result:
```
[Test worker] INFO org.hypertrace.core.query.service.QueryClientTest - traceId: "G\025\235\255O\306i\270\225\332wL\036\231\245\234"
spanId: "+q\346|\2175\245\207"
process: "{service_name=frontend, tags=[]}"
operationName: "Sent.hipstershop.ProductCatalogService.ListProducts"
```
