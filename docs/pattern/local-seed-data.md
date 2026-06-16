# Local Seed Data Pattern

## Decision

Use SQL seed data for local load-test preparation.

Seed SQL path:

```text
src/main/resources/db/local/seed-coupon-lab.sql
```

## Seed Data

The SQL creates:

- 10,000 dummy accounts
- one coupon named `load-test-coupon`
- coupon quantity of 1,000

Dummy accounts use predictable email and nickname values.

```text
load-test-user-1@example.com
load-test-user-2@example.com
...
load-test-user-10000@example.com
```

## Rationale

The project does not need signup or authentication for the waiting queue experiment.

Prebuilt dummy accounts keep the benchmark focused on the target flow:

```text
many users -> Redis waiting queue -> limited DB entry -> coupon issue
```

## Run

Run the SQL against the local MySQL container after the application has created the schema.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml exec -T mysql \
  mysql -uroot -p$MYSQL_ROOT_PASSWORD traffic_queue_lab < src/main/resources/db/local/seed-coupon-lab.sql
```
