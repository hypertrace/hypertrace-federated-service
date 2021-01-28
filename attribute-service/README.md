# Attribute Service

Attribute service provides CRUD operations for the attributes in Hypertrace.

## What are Attributes?
An attribute is a granular piece of information that is present in Hypertrace and available for querying/displaying in the UI. An attribute encapsulates information like 1) the type of the data, 2) kind of aggregations allowed on it, 3) underlying service/source that can serve this data etc.
This helps in building a generic framework of communication between the UI and the backend and also assists the backend to fetch data from the appropriate sources in a generic way.

Ex.
```
key: apiName,
value_kind: TYPE_STRING,
groupable : true,
display_name: Endpoint Name,
scope: API_TRACE,
sources: [QS],
type: ATTRIBUTE
```
While hypertrace offers a simple service model, it also permits storing certain fields in different backends. Attributes include tags to help locate the service hosting their corresponding data.

For example, `apiName` is generated from raw trace data streams. `sources: [QS]`  communicates this, as **Q**uery **S**ervice is the api that queries that data (via Pinot).

You can check out structure of attribute [here](https://github.com/hypertrace/attribute-service/blob/main/attribute-service-api/src/main/proto/org/hypertrace/core/attribute/service/v1/attribute_metadata.proto).

### How are attributes created?
An initial list of attributes needed by Hypertrace are seeded from `helm/configs` but they can also be dynamically registered and queried using the APIs of AttributeService.

## Description

Attribute service is a part of query architecture in Hypertrace and here is the use of it in context of its callers: [hypertrace-graphql](https://github.com/hypertrace/hypertrace-graphql) and [gateway-service](https://github.com/hypertrace/gateway-service). 

| ![space-1.jpg](https://hypertrace-docs.s3.amazonaws.com/hypertrace-query-arch.png) | 
|:--:| 
| *Hypertrace Query Architecture* |

Attribute service fetches all attributes relevant to the scope of what is being shown on UI from Mongo and where it also stores attributes. All of this metadata assists the UI and backend to perform things in a generic fashion, like
- All String attributes in the UI can have a different look at feel and also different operations that can be supported on them(in the explorer).
- The backend also uses part of the information(like the sources) to figure out where to fetch data from i.e. query-service vs entity-service.

## Building locally
The Attribute service uses gradlew to compile/install/distribute. Gradle wrapper is already part of the source code. To build Attribute Service, run:

```
./gradlew dockerBuildImages
```

## Testing

### Running unit tests
Run `./gradlew test` to execute unit tests. 

### Running integration tests

Run `./gradlew integrationTest` to execute integration tests. 


### Testing image

To test your image using the docker-compose setup follow the steps:

- Commit you changes to a branch say `attribute-service-test`.
- Go to [hypertrace-service](https://github.com/hypertrace/hypertrace-service) and checkout the above branch in the submodule.
```
cd attribute-service && git checkout attribute-service-test && cd ..
```
- Change tag for `hypertrace-service` from `:main` to `:test` in [docker-compose file](https://github.com/hypertrace/hypertrace/blob/main/docker/docker-compose.yml) like this.

```yaml
  hypertrace-service:
    image: hypertrace/hypertrace-service:test
    container_name: hypertrace-service
    ...
```
- and then run `docker-compose up` to test the setup.

## Docker Image Source:
- [DockerHub > Attribute service](https://hub.docker.com/r/hypertrace/attribute-service)



