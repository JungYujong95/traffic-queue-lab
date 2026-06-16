# Local Docker Compose Architecture

## Overview

The local environment runs one MySQL container, one Redis container, and four Spring Boot application containers.

Spring Boot instances are exposed on ports `8080`, `8081`, `8082`, and `8083`.

## Components

- `mysql`: local relational database for JPA persistence.
- `redis`: local Redis instance for the waiting queue experiment.
- `app-8080` to `app-8083`: independent Spring Boot containers using the `local` profile.

## Configuration

Common Spring settings stay in `src/main/resources/application.yml`.

Local infrastructure settings stay in `src/main/resources/application-local.yml` and are injected from `.env.local` through Docker Compose environment variables.

The Hikari maximum pool size defaults to `10` to intentionally create a constrained database access path for load testing.

## Run Infrastructure Only

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml up -d
```

This starts only MySQL and Redis.

## Run Infrastructure and Backends

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml --profile backend up --build
```

This starts MySQL, Redis, and four Spring Boot containers.

## Local Resource Limits

Each backend container defaults to `512m` of memory. The JVM uses `JAVA_TOOL_OPTIONS` with `MaxRAMPercentage=60.0`, so the heap stays inside the container limit instead of expanding against the host memory.

MySQL defaults to `512m` and Redis defaults to `128m`.

Run commands from the project root.

See `docker/README.md` for detailed Docker commands and memory tuning examples.
