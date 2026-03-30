#!/bin/bash
# 压力测试执行脚本 - Linux/Mac
# 使用方法：./run-loadtest.sh [并发用户数] [测试时长秒]

USERS=${1:-50}
DURATION=${2:-30}

echo "============================================"
echo "  用户管理系统 - 压力测试执行脚本"
echo "============================================"
echo
echo "配置:"
echo "  并发用户数：$USERS"
echo "  测试时长：${DURATION}秒"
echo

# 检查 k6 是否安装
if command -v k6 &> /dev/null; then
    echo "[INFO] 使用 k6 进行压力测试..."
    echo
    k6 run --vus "$USERS" --duration "${DURATION}s" tests/performance/k6-login.js
    exit $?
fi

# 检查 Python 是否安装
if command -v python3 &> /dev/null; then
    echo "[INFO] 使用 Python 进行压力测试..."
    echo
    python3 tests/performance/loadtest.py --users "$USERS" --duration "$DURATION"
    exit $?
fi

if command -v python &> /dev/null; then
    echo "[INFO] 使用 Python 进行压力测试..."
    echo
    python tests/performance/loadtest.py --users "$USERS" --duration "$DURATION"
    exit $?
fi

echo "[ERROR] 未找到 k6 或 Python，无法执行压力测试"
echo
echo "请安装以下工具之一:"
echo "  1. k6: https://k6.io/docs/getting-started/installation/"
echo "  2. Python: https://www.python.org/downloads/"
echo
