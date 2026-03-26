# Quickstart: AI Show Scheduling Bounded Context

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26

## Prerequisites

- Java 25+
- Maven 3.9+
- `ANTHROPIC_API_KEY` environment variable set (for AI agent functionality)

## Build & Run

```bash
# Build all modules (including new scheduling module)
mvn -B package --file pom.xml

# Run the application with local profile
cd scheduling
mvn spring-boot:run -Dspring-boot.run.profiles=default,local
```

## Verify

### AI Agent Interaction

```bash
# Send a scheduling request to the AI agent
curl -X POST http://localhost:8080/scheduling/agent/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "Schedule a show for Movie 1 in Hall 1 tomorrow at 7 PM"}'
```

### Show Queries

```bash
# List scheduled shows
curl http://localhost:8080/shows

# Get show details
curl http://localhost:8080/shows/S00000000000000001
```

## Key Configuration

```yaml
# application-default.yaml
spring:
  jpa:
    mapping-resources:
      - META-INF/domain/show.orm.xml
      - META-INF/query/show.orm.xml
      - seedwork/META-INF/domain/super-types.orm.xml

# application-local.yaml
anthropic:
  api-key: ${ANTHROPIC_API_KEY}
```

## Module Dependencies

```
scheduling → seedwork (DDD base classes, event infrastructure)
scheduling → anthropic-java (Claude LLM SDK)
scheduling ✗ booking (no dependency — bounded context independence)
```

## Architecture Verification

```bash
# Run ArchUnit tests to verify layering
mvn -B test -pl scheduling -Dtest=ArchitectureTest
```
