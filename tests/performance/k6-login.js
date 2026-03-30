/**
 * k6 压力测试脚本 - 登录接口
 *
 * 目标性能指标:
 * - 登录接口 P95 < 100ms
 * - 吞吐量 >= 10,000 TPS
 *
 * 使用方法:
 *   k6 run --vus 100 --duration 30s k6-login.js
 *   k6 run --scenario login-stress k6-login.js
 *
 * @author UserManagement Team
 * @since 1.0.0
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ============================================================================
// 自定义指标
// ============================================================================

// 成功率
const loginSuccessRate = new Rate('login_success_rate');

// P95 响应时间
const loginP95 = new Trend('login_p95');

// 成功登录计数
const successLogins = new Counter('success_logins');

// 失败登录计数
const failedLogins = new Counter('failed_logins');

// ============================================================================
// 测试配置
// ============================================================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

// 测试用户凭证 (请根据实际情况修改)
const TEST_USERS = [
    { email: 'test1@example.com', password: 'Test1234!' },
    { email: 'test2@example.com', password: 'Test1234!' },
    { email: 'test3@example.com', password: 'Test1234!' },
    { email: 'test4@example.com', password: 'Test1234!' },
    { email: 'test5@example.com', password: 'Test1234!' },
];

// ============================================================================
// k6 场景配置
// ============================================================================

export const options = {
    // 通用配置
    thresholds: {
        'http_req_duration': ['p(95)<100'], // P95 < 100ms
        'login_success_rate': ['>0.99'],    // 成功率 > 99%
        'login_p95': ['p(95)<100'],         // 自定义 P95 指标
    },

    // 场景定义
    scenarios: {
        // 场景 1: 基准测试
        baseline: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },   // 热身：0 -> 10 VUs
                { duration: '1m', target: 10 },    // 稳定：10 VUs
                { duration: '30s', target: 0 },    // 冷却：10 -> 0 VUs
            ],
            gracefulRampDown: '30s',
        },

        // 场景 2: 压力测试
        stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 50 },    // 快速上升到 50 VUs
                { duration: '2m', target: 50 },    // 稳定在 50 VUs
                { duration: '1m', target: 100 },   // 上升到 100 VUs
                { duration: '2m', target: 100 },   // 稳定在 100 VUs
                { duration: '1m', target: 200 },   // 上升到 200 VUs
                { duration: '3m', target: 200 },   // 稳定在 200 VUs
                { duration: '1m', target: 0 },     // 冷却
            ],
            gracefulRampDown: '30s',
        },

        // 场景 3: 峰值测试
        spike: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 500 },  // 快速峰值到 500 VUs
                { duration: '30s', target: 500 },  // 保持峰值
                { duration: '30s', target: 0 },    // 快速下降
            ],
            gracefulRampDown: '30s',
        },

        // 场景 4: 耐力测试
        endurance: {
            executor: 'constant-vus',
            vus: 50,
            duration: '10m',
            gracefulStop: '30s',
        },
    },
};

// ============================================================================
// 辅助函数
// ============================================================================

/**
 * 获取随机测试用户
 */
function getRandomUser() {
    const index = Math.floor(Math.random() * TEST_USERS.length);
    return TEST_USERS[index];
}

/**
 * 生成唯一请求 ID
 */
function generateRequestId() {
    return `login_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

// ============================================================================
// 测试主函数
// ============================================================================

export default function () {
    const user = getRandomUser();
    const requestId = generateRequestId();

    // 请求头
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'X-Request-ID': requestId,
    };

    // 登录请求 payload
    const payload = JSON.stringify({
        email: user.email,
        password: user.password,
    });

    // 执行登录请求
    const startTime = Date.now();
    const response = http.post(`${BASE_URL}/auth/login`, payload, {
        headers: headers,
        tags: { name: 'login' },
    });
    const endTime = Date.now();

    // 记录响应时间
    const responseTime = endTime - startTime;
    loginP95.add(responseTime);

    // 验证响应
    const isSuccess = check(response, {
        'status is 200': (r) => r.status === 200,
        'has access_token': (r) => r.json('data.accessToken') !== null,
        'has refresh_token': (r) => r.json('data.refreshToken') !== null,
        'response time < 100ms': (r) => responseTime < 100,
    });

    // 记录成功/失败
    if (isSuccess) {
        loginSuccessRate.add(1);
        successLogins.add(1);
    } else {
        loginSuccessRate.add(0);
        failedLogins.add(1);

        // 记录错误详情
        console.log(`登录失败：status=${response.status}, body=${response.body}`);
    }

    // 思考时间 (模拟真实用户行为)
    sleep(0.1 + Math.random() * 0.2);
}

// ============================================================================
// 钩子函数
// ============================================================================

/**
 * 测试开始前执行
 */
export function handleSummary(data) {
    return {
        'summary.json': JSON.stringify(data, null, 2),
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
    };
}

function textSummary(data, options) {
    const { metrics } = data;
    const p95 = metrics.login_p95?.values?.['p(95)'] || 0;
    const successRate = metrics.login_success_rate?.values?.rate || 0;
    const successCount = metrics.success_logins?.values?.count || 0;
    const failCount = metrics.failed_logins?.values?.count || 0;

    return `
=====================================
  登录压力测试结果
=====================================
  成功登录数：${successCount}
  失败登录数：${failCount}
  成功率：${(successRate * 100).toFixed(2)}%
  P95 响应时间：${p95.toFixed(2)}ms
=====================================`;
}
