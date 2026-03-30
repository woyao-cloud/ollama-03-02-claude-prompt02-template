@echo off
REM 压力测试执行脚本 - Windows
REM 使用方法：run-loadtest.bat [并发用户数] [测试时长秒]

set USERS=%1
set DURATION=%2

if "%USERS%"=="" set USERS=50
if "%DURATION%"=="" set DURATION=30

echo ============================================
echo   用户管理系统 - 压力测试执行脚本
echo ============================================
echo.
echo 配置:
echo   并发用户数：%USERS%
echo   测试时长：%DURATION%秒
echo.

REM 检查 k6 是否安装
where k6 >nul 2>&1
if %errorlevel%==0 (
    echo [INFO] 使用 k6 进行压力测试...
    echo.
    k6 run --vus %USERS% --duration %DURATION%s tests\performance\k6-login.js
    goto :end
)

REM 检查 Python 是否安装
python --version >nul 2>&1
if %errorlevel%==0 (
    echo [INFO] 使用 Python 进行压力测试...
    echo.
    python tests\performance\loadtest.py --users %USERS% --duration %DURATION%
    goto :end
)

REM 检查 Python3 是否安装
python3 --version >nul 2>&1
if %errorlevel%==0 (
    echo [INFO] 使用 Python3 进行压力测试...
    echo.
    python3 tests\performance\loadtest.py --users %USERS% --duration %DURATION%
    goto :end
)

echo [ERROR] 未找到 k6 或 Python，无法执行压力测试
echo.
echo 请安装以下工具之一:
echo   1. k6: choco install k6
echo   2. Python: https://www.python.org/downloads/
echo.

:end
pause
