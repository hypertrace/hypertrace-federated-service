# Hypertrace GraphQL
Hypertrace GraphQL service serves the GraphQL API which will be used by Hypertrace UI.

## Testing

`./gradlew test`

## Running

`./gradlew run`

## Queries
Here are some of the important GraphQL queries:


### 1. Verify trace exists

```graphql
curl -s localhost:2020/graphql -H 'Content-Type: application/graphql' -d \
'{
  traces(
    type: API_TRACE
    between: {
      startTime: "2015-01-01T00:00:00.000Z"
      endTime: "2025-01-01T00:00:00.000Z"
    }
    filterBy: [
      {
        operator: EQUALS
        value: "348bae39282251a5"
        type: ID
        idType: API_TRACE
      }
    ]
  ) {
    total
  }
}'
```


### 2. Get all service names

```graphql
curl -s localhost:2020/graphql  -H 'Content-Type: application/graphql' -d \
'{
  entities(
    type: SERVICE 
    between: {
      startTime: "2015-01-01T00:00:00Z"
      endTime: "2025-01-01T00:00:00Z"
    }
  ) {
    results {
      name: attribute(key: "name")
    }
    total
  }
}'
```

### 3. Get all backend names

```graphql
curl -s localhost:2020/graphql -H 'Content-Type: application/graphql' -d \
'{
  entities(
    type: BACKEND
    between: {
      startTime: "2015-01-01T00:00:00Z"
      endTime: "2025-01-01T00:00:00Z"
    }
  ) {
    results {
      name: attribute(key: "name")
    }
    total
  }
}'
```

### 4. Get all API names

```graphql
curl -s localhost:2020/graphql -H 'Content-Type: application/graphql' -d \
'{
  entities(
    type: API
    between: {
      startTime: "2015-01-01T00:00:00Z"
      endTime: "2025-01-01T00:00:00Z"
    }
  ) {
    results {
      name: attribute(key: "name")
    }
    total
  }
}'
```

### 5. Get service and backend dependency graph

```graphql
curl -s localhost:2020/graphql -H 'Content-Type: application/graphql' -d \
'{
  entities(
    type: SERVICE
    between: {
      startTime: "2015-01-01T00:00:00.000Z"
      endTime: "2025-01-01T00:00:00.000Z"
    }
  ) {
    results {
      id
      name: attribute(key: "name")
      outgoingEdges_SERVICE: outgoingEdges(neighborType: SERVICE) {
        results {
          neighbor {
            name: attribute(key: "name")
          }
        }
      }
      outgoingEdges_BACKEND: outgoingEdges(neighborType: BACKEND) {
        results {
          neighbor {
            name: attribute(key: "name")
          }
        }
      }
      incomingEdges_SERVICE: incomingEdges(neighborType: SERVICE) {
        results {
          neighbor {
            name: attribute(key: "name")
          }
        }
      }
    }
  }
}'
```