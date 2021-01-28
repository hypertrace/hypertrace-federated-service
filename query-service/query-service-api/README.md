## Generating Golang Client with GRPC support
The client currently can be generated locally by changing following properties in build.gradle.kts:
```kotlin
val generateLocalGoGrpcFiles = true

path = "<go-path>/bin/protoc-gen-go"

```

Next run ../gradlew clean build

The go files are generated in build/generated/source/proto/main/*go directories.
