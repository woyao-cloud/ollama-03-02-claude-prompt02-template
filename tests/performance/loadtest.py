#!/usr/bin/env python3
"""
压力测试脚本 - 登录接口
使用 Python + aiohttp 进行并发压力测试

目标性能指标:
- 登录接口 P95 < 100ms
- 吞吐量 >= 10,000 TPS

使用方法:
    python loadtest.py --users 100 --duration 30

依赖:
    pip install aiohttp aiohttp[speedups] statistics
"""

import asyncio
import aiohttp
import time
import statistics
import argparse
import json
from datetime import datetime
from typing import List, Dict, Any
from dataclasses import dataclass, asdict


@dataclass
class TestResult:
    """单次请求结果"""
    success: bool
    response_time_ms: float
    status_code: int
    error: str = ""


@dataclass
class TestSummary:
    """测试汇总"""
    total_requests: int
    successful_requests: int
    failed_requests: int
    success_rate: float
    duration_seconds: float
    requests_per_second: float
    mean_response_time_ms: float
    median_response_time_ms: float
    p95_response_time_ms: float
    p99_response_time_ms: float
    min_response_time_ms: float
    max_response_time_ms: float
    timestamp: str


# 测试配置
BASE_URL = "http://localhost:8080/api"
TEST_USERS = [
    {"email": "test1@example.com", "password": "Test1234!"},
    {"email": "test2@example.com", "password": "Test1234!"},
    {"email": "test3@example.com", "password": "Test1234!"},
    {"email": "test4@example.com", "password": "Test1234!"},
    {"email": "test5@example.com", "password": "Test1234!"},
]


async def login_request(
    session: aiohttp.ClientSession,
    user: Dict[str, str],
    semaphore: asyncio.Semaphore
) -> TestResult:
    """执行单次登录请求"""
    async with semaphore:
        start_time = time.perf_counter()
        try:
            payload = {
                "email": user["email"],
                "password": user["password"]
            }
            headers = {
                "Content-Type": "application/json",
                "Accept": "application/json"
            }

            async with session.post(
                f"{BASE_URL}/auth/login",
                json=payload,
                headers=headers,
                timeout=aiohttp.ClientTimeout(total=10)
            ) as response:
                end_time = time.perf_counter()
                response_time_ms = (end_time - start_time) * 1000

                if response.status == 200:
                    data = await response.json()
                    if data.get("data", {}).get("accessToken"):
                        return TestResult(
                            success=True,
                            response_time_ms=response_time_ms,
                            status_code=response.status
                        )

                return TestResult(
                    success=False,
                    response_time_ms=response_time_ms,
                    status_code=response.status,
                    error=f"Status: {response.status}"
                )

        except asyncio.TimeoutError:
            end_time = time.perf_counter()
            return TestResult(
                success=False,
                response_time_ms=(end_time - start_time) * 1000,
                status_code=0,
                error="Timeout"
            )
        except Exception as e:
            end_time = time.perf_counter()
            return TestResult(
                success=False,
                response_time_ms=(end_time - start_time) * 1000,
                status_code=0,
                error=str(e)
            )


async def run_load_test(
    num_users: int,
    duration_seconds: int,
    requests_per_user: int = None
) -> List[TestResult]:
    """运行压力测试"""

    if requests_per_user is None:
        # 根据并发用户数和持续时间计算请求数
        requests_per_user = max(10, duration_seconds // 2)

    semaphore = asyncio.Semaphore(num_users)
    results: List[TestResult] = []

    async with aiohttp.ClientSession() as session:
        start_time = time.time()
        end_time = start_time + duration_seconds

        tasks = []
        request_count = 0

        while time.time() < end_time:
            # 选择随机用户
            user = TEST_USERS[request_count % len(TEST_USERS)]

            # 创建请求任务
            task = asyncio.create_task(
                login_request(session, user, semaphore)
            )
            tasks.append(task)
            request_count += 1

            # 控制请求速率
            await asyncio.sleep(0.001)  # 1ms 间隔

            # 定期收集结果
            if len(tasks) >= num_users * 2:
                done, pending = await asyncio.wait(
                    tasks,
                    timeout=0.1,
                    return_when=asyncio.FIRST_COMPLETED
                )
                results.extend([r for r in done])
                tasks = list(pending)

        # 等待所有剩余任务完成
        if tasks:
            done, _ = await asyncio.wait(tasks, timeout=5)
            results.extend([r for r in done])

    return results


def calculate_summary(results: List[TestResult], duration: float) -> TestSummary:
    """计算测试汇总"""
    response_times = [r.response_time_ms for r in results if r.success or r.response_time_ms > 0]
    successful = sum(1 for r in results if r.success)

    if not response_times:
        return TestSummary(
            total_requests=len(results),
            successful_requests=successful,
            failed_requests=len(results) - successful,
            success_rate=0,
            duration_seconds=duration,
            requests_per_second=0,
            mean_response_time_ms=0,
            median_response_time_ms=0,
            p95_response_time_ms=0,
            p99_response_time_ms=0,
            min_response_time_ms=0,
            max_response_time_ms=0,
            timestamp=datetime.now().isoformat()
        )

    sorted_times = sorted(response_times)
    n = len(sorted_times)

    return TestSummary(
        total_requests=len(results),
        successful_requests=successful,
        failed_requests=len(results) - successful,
        success_rate=successful / len(results) if results else 0,
        duration_seconds=duration,
        requests_per_second=len(results) / duration if duration > 0 else 0,
        mean_response_time_ms=statistics.mean(response_times),
        median_response_time_ms=statistics.median(response_times),
        p95_response_time_ms=sorted_times[int(n * 0.95)] if n > 1 else sorted_times[-1],
        p99_response_time_ms=sorted_times[int(n * 0.99)] if n > 1 else sorted_times[-1],
        min_response_time_ms=min(response_times),
        max_response_time_ms=max(response_times),
        timestamp=datetime.now().isoformat()
    )


def print_summary(summary: TestSummary):
    """打印测试汇总"""
    print("\n" + "=" * 60)
    print("  压力测试结果")
    print("=" * 60)
    print(f"  总请求数：      {summary.total_requests:,}")
    print(f"  成功请求数：    {summary.successful_requests:,}")
    print(f"  失败请求数：    {summary.failed_requests:,}")
    print(f"  成功率：        {summary.success_rate * 100:.2f}%")
    print(f"  测试时长：      {summary.duration_seconds:.1f}秒")
    print(f"  吞吐量：        {summary.requests_per_second:.0f} TPS")
    print("-" * 60)
    print(f"  平均响应时间：  {summary.mean_response_time_ms:.2f}ms")
    print(f"  中位数响应时间：{summary.median_response_time_ms:.2f}ms")
    print(f"  P95 响应时间：   {summary.p95_response_time_ms:.2f}ms")
    print(f"  P99 响应时间：   {summary.p99_response_time_ms:.2f}ms")
    print(f"  最小响应时间：  {summary.min_response_time_ms:.2f}ms")
    print(f"  最大响应时间：  {summary.max_response_time_ms:.2f}ms")
    print("=" * 60)

    # 性能目标检查
    print("\n  性能目标检查:")
    p95_status = "✅ PASS" if summary.p95_response_time_ms < 100 else "❌ FAIL"
    tps_status = "✅ PASS" if summary.requests_per_second >= 10000 else "❌ FAIL"
    success_status = "✅ PASS" if summary.success_rate > 0.99 else "❌ FAIL"

    print(f"    P95 < 100ms:    {p95_status} ({summary.p95_response_time_ms:.2f}ms)")
    print(f"    TPS >= 10,000:  {tps_status} ({summary.requests_per_second:.0f})")
    print(f"    成功率 > 99%:   {success_status} ({summary.success_rate * 100:.2f}%)")
    print("=" * 60 + "\n")


def main():
    parser = argparse.ArgumentParser(description="登录接口压力测试")
    parser.add_argument(
        "--users", "-u",
        type=int,
        default=50,
        help="并发用户数 (默认：50)"
    )
    parser.add_argument(
        "--duration", "-d",
        type=int,
        default=30,
        help="测试时长 (秒，默认：30)"
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default="loadtest-results.json",
        help="结果输出文件 (默认：loadtest-results.json)"
    )
    parser.add_argument(
        "--url",
        type=str,
        default=BASE_URL,
        help=f"目标 URL (默认：{BASE_URL})"
    )

    args = parser.parse_args()

    # 更新全局配置
    global BASE_URL
    BASE_URL = args.url

    print(f"\n开始压力测试...")
    print(f"  并发用户数：{args.users}")
    print(f"  测试时长：{args.duration}秒")
    print(f"  目标 URL: {args.url}")
    print(f"  测试用户数：{len(TEST_USERS)}")

    # 运行测试
    start_time = time.time()
    results = asyncio.run(run_load_test(args.users, args.duration))
    duration = time.time() - start_time

    # 计算并打印汇总
    summary = calculate_summary(results, duration)
    print_summary(summary)

    # 保存结果到文件
    output_data = {
        "summary": asdict(summary),
        "config": {
            "concurrent_users": args.users,
            "duration_seconds": args.duration,
            "target_url": args.url,
            "test_users_count": len(TEST_USERS)
        }
    }

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(output_data, f, indent=2, ensure_ascii=False)

    print(f"结果已保存到：{args.output}")

    # 返回退出码
    if summary.p95_response_time_ms < 100 and summary.success_rate > 0.99:
        return 0
    return 1


if __name__ == "__main__":
    exit(main())
