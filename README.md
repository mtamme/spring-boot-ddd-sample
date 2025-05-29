# Spring Boot DDD Sample

[![Build](https://github.com/mtamme/spring-boot-ddd-sample/actions/workflows/build.yaml/badge.svg)](https://github.com/mtamme/spring-boot-ddd-sample/actions/workflows/build.yaml)

This is the sample bounded context from the [<CODE/CRAFTS>](https://code-crafts.com) conference
workshop [From Strategy to Tactics – Kickstart Your DDD Journey Collaboratively](https://2025.code-crafts.com/workshop-from-strategy-to-tactics).

## Usage

### Requirements

- Java 25

### Build

Build and run the tests:

```shell
./mvnw clean package
```

### Run

Start the sample bounded context:

```shell
java -jar booking/target/booking-1.0.0-SNAPSHOT.jar
```

### Explore

Once the service is running, you can browse the API and interact with the sample bounded context
via [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
