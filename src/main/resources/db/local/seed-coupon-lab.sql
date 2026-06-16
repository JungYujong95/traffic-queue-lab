-- Local load-test seed data for MySQL 8.
-- Creates 10,000 dummy accounts and one limited coupon.

SET @account_count = 10000;
SET @coupon_name = 'load-test-coupon';
SET @coupon_total_quantity = 1000;
SET SESSION cte_max_recursion_depth = 10000;

DELETE FROM coupon_issues
WHERE coupon_id = 1;

INSERT INTO coupons (
    id,
    name,
    total_quantity,
    issued_quantity,
    started_at,
    ended_at,
    created_at
)
VALUES (
    1,
    @coupon_name,
    @coupon_total_quantity,
    0,
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 30 DAY,
    NOW()
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    total_quantity = VALUES(total_quantity),
    issued_quantity = 0,
    started_at = VALUES(started_at),
    ended_at = VALUES(ended_at);

INSERT INTO accounts (
    email,
    nickname,
    created_at
)
WITH RECURSIVE numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM numbers
    WHERE number < @account_count
)
SELECT
    CONCAT('load-test-user-', number, '@example.com'),
    CONCAT('load-test-user-', number),
    NOW()
FROM numbers
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname);
