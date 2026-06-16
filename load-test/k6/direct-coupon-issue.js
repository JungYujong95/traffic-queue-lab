import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

export const options = {
  scenarios: {
    direct_coupon_issue: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 500 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.50'],
    http_req_duration: ['p(95)<5000'],
    direct_issue_success_rate: ['rate>0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = Number(__ENV.COUPON_ID || 1);
const ACCOUNT_COUNT = Number(__ENV.ACCOUNT_COUNT || 10000);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.1);

const successCount = new Counter('direct_issue_success_count');
const soldOutCount = new Counter('direct_issue_sold_out_count');
const duplicateCount = new Counter('direct_issue_duplicate_count');
const notFoundCount = new Counter('direct_issue_not_found_count');
const unexpectedCount = new Counter('direct_issue_unexpected_count');
const successRate = new Rate('direct_issue_success_rate');

export default function () {
  const accountId = nextAccountId();
  const response = issueCoupon(accountId);

  const result = classifyResponse(response);
  recordMetrics(result);

  check(response, {
    'response is expected status': () => result !== 'unexpected',
  });

  sleep(THINK_TIME_SECONDS);
}

function nextAccountId() {
  return ((__VU + __ITER) % ACCOUNT_COUNT) + 1;
}

function issueCoupon(accountId) {
  return http.post(
    `${BASE_URL}/api/v1/coupons/${COUPON_ID}/issue/direct`,
    null,
    {
      headers: {
        'X-Account-Id': String(accountId),
      },
      tags: {
        api: 'direct-coupon-issue',
      },
    }
  );
}

function classifyResponse(response) {
  if (response.status === 200) {
    return 'success';
  }

  if (response.status === 404) {
    return 'not_found';
  }

  if (response.status === 409) {
    return classifyConflict(response);
  }

  return 'unexpected';
}

function classifyConflict(response) {
  const body = parseJson(response);

  if (body?.code === 'COUPON_409_001') {
    return 'sold_out';
  }

  if (body?.code === 'COUPON_409_002') {
    return 'duplicate';
  }

  return 'unexpected';
}

function parseJson(response) {
  try {
    return response.json();
  } catch (error) {
    return null;
  }
}

function recordMetrics(result) {
  successRate.add(result === 'success');

  if (result === 'success') {
    successCount.add(1);
    return;
  }

  if (result === 'sold_out') {
    soldOutCount.add(1);
    return;
  }

  if (result === 'duplicate') {
    duplicateCount.add(1);
    return;
  }

  if (result === 'not_found') {
    notFoundCount.add(1);
    return;
  }

  unexpectedCount.add(1);
}
