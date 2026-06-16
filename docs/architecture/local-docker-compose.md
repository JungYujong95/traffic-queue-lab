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

## Run

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml up --build
```

Run the command from the project root.
