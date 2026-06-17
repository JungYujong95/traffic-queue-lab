import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

export const options = {
  scenarios: {
    wait_coupon_issue: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.KTX_RPS || 500),
      timeUnit: '1s',
      duration: __ENV.KTX_DURATION || '2m',
      preAllocatedVUs: Number(__ENV.KTX_PREALLOCATED_VUS || 500),
      maxVUs: Number(__ENV.KTX_MAX_VUS || 1500),
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<5000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = Number(__ENV.COUPON_ID || 1);
const ACCOUNT_COUNT = Number(__ENV.ACCOUNT_COUNT || 100000);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.1);

const waitingCount = new Counter('wait_issue_waiting_count');
const processingCount = new Counter('wait_issue_processing_count');
const issuedCount = new Counter('wait_issue_issued_count');
const duplicateCount = new Counter('wait_issue_duplicate_count');
const soldOutCount = new Counter('wait_issue_sold_out_count');
const failedCount = new Counter('wait_issue_failed_count');
const unexpectedCount = new Counter('wait_issue_unexpected_count');
const acceptedRate = new Rate('wait_issue_accepted_rate');

export default function () {
  const accountId = nextAccountId();
  const response = register(accountId);

  const result = classifyResponse(response);
  recordMetrics(result);

  check(response, {
    'response is expected status': () => result !== 'unexpected',
  });

  sleep(THINK_TIME_SECONDS);
}

function nextAccountId() {
  return ((__VU * 10007) + __ITER) % ACCOUNT_COUNT + 1;
}

function register(accountId) {
  return http.post(
    `${BASE_URL}/api/v1/coupons/${COUPON_ID}/issue/wait`,
    null,
    {
      headers: {
        'X-Account-Id': String(accountId),
      },
      tags: {
        api: 'wait-coupon-issue',
      },
    }
  );
}

function classifyResponse(response) {
  if (response.status !== 200) {
    return 'unexpected';
  }

  const body = parseJson(response);
  const status = body?.data?.status;

  if (status === 'WAITING') {
    return 'waiting';
  }

  if (status === 'PROCESSING') {
    return 'processing';
  }

  if (status === 'ISSUED') {
    return 'issued';
  }

  if (status === 'DUPLICATE') {
    return 'duplicate';
  }

  if (status === 'SOLD_OUT') {
    return 'sold_out';
  }

  if (status === 'FAILED') {
    return 'failed';
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
  acceptedRate.add(result !== 'unexpected');

  if (result === 'waiting') {
    waitingCount.add(1);
    return;
  }

  if (result === 'processing') {
    processingCount.add(1);
    return;
  }

  if (result === 'issued') {
    issuedCount.add(1);
    return;
  }

  if (result === 'duplicate') {
    duplicateCount.add(1);
    return;
  }

  if (result === 'sold_out') {
    soldOutCount.add(1);
    return;
  }

  if (result === 'failed') {
    failedCount.add(1);
    return;
  }

  unexpectedCount.add(1);
}
