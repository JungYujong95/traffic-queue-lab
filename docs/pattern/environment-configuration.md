# Environment Configuration Pattern

## Decision

Use profile-specific Spring configuration for infrastructure values.

- `application.yml`: common application configuration only.
- `application-local.yml`: local MySQL, Redis, JPA, and Hikari settings.
- `.env.local`: local Docker Compose and Spring environment variables.
- `.env.local.example`: shareable template for local environment variables.

## Rationale

Database and Redis endpoints differ by environment, so they should not be hardcoded in common configuration.

This keeps the local bottleneck setup explicit while allowing future test, staging, or production profiles to define different infrastructure values without changing application code.

Docker Compose overrides `SPRING_DATASOURCE_URL` and `SPRING_DATA_REDIS_HOST` for containers so they can reach `mysql` and `redis` by service name. The `.env.local` defaults target host-based local execution.

`application.yml` uses `local` as the default profile for this lab project, so IntelliJ can start the application against Dockerized local infrastructure without extra profile setup.

Backend containers are assigned to the `backend` Compose profile. Default local startup can run only MySQL and Redis, while load-test startup can opt in to all four Spring Boot containers.

Container memory limits are also configured through `.env.local`. Backend JVM heap sizing uses `JAVA_TOOL_OPTIONS` so heap growth follows the container memory limit.

`.env.local` is ignored by Git. Commit the example file, not local secrets.
