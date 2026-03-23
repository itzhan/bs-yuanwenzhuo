@echo off
chcp 65001 >nul 2>nul
title 药店药品信息管理系统 — Docker 启动
setlocal enabledelayedexpansion

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║   💊 药店药品信息管理系统 — Docker 启动                 ║
echo  ╚══════════════════════════════════════════════════════╝
echo.

:: ── 检查 Docker ──
where docker >nul 2>nul
if errorlevel 1 (
  echo  [✗] Docker 未安装，请先安装 Docker Desktop
  pause
  exit /b 1
)

docker info >nul 2>nul
if errorlevel 1 (
  echo  [✗] Docker 未运行，请先启动 Docker Desktop
  pause
  exit /b 1
)
echo  [✓] Docker 运行正常

:: ── 启动容器 ──
echo.
echo  🚀 启动 Docker 容器...
echo.
docker compose up -d

:: ── 等待服务就绪 ──
echo.
echo  ⏳ 等待服务就绪（约60秒）...
timeout /t 60 >nul

echo.
echo  ╔══════════════════════════════════════════════════════════════╗
echo  ║  ✅ 所有服务已启动                                          ║
echo  ╠══════════════════════════════════════════════════════════════╣
echo  ║                                                              ║
echo  ║  📡 服务地址                                                 ║
echo  ║  后端 API     →  http://localhost:8080                       ║
echo  ║  用户端       →  http://localhost:3000                       ║
echo  ║  管理端       →  http://localhost:9527                       ║
echo  ║                                                              ║
echo  ╠══════════════════════════════════════════════════════════════╣
echo  ║                                                              ║
echo  ║  👤 测试账号                                                 ║
echo  ║  ┌────────────┬──────────────────┬──────────────┐            ║
echo  ║  │ 角色       │ 用户名           │ 密码         │            ║
echo  ║  ├────────────┼──────────────────┼──────────────┤            ║
echo  ║  │ 管理员     │ admin            │ 123456       │            ║
echo  ║  │ 药剂师     │ pharmacist1      │ 123456       │            ║
echo  ║  │ 仓库管理员 │ warehouse1       │ 123456       │            ║
echo  ║  └────────────┴──────────────────┴──────────────┘            ║
echo  ║                                                              ║
echo  ╠══════════════════════════════════════════════════════════════╣
echo  ║  停止服务: docker compose down                               ║
echo  ║  查看日志: docker compose logs -f                            ║
echo  ╚══════════════════════════════════════════════════════════════╝
echo.
pause
