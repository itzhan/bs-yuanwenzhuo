@echo off
chcp 65001 >nul 2>nul
title 药店药品信息管理系统 — 启动
setlocal enabledelayedexpansion

:: ═══════════════════════════════════════════════
::   药店药品信息管理系统 — 一键启动脚本 (Windows)
:: ═══════════════════════════════════════════════

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=pharmacy_db
set DB_USER=root
set DB_PASS=ab123168
set BACKEND_PORT=8080
set ADMIN_PORT=9527
set FRONTEND_PORT=3000
set SCRIPT_DIR=%~dp0

echo.
echo  ╔══════════════════════════════════════════════╗
echo  ║   💊 药店药品信息管理系统                        ║
echo  ╚══════════════════════════════════════════════╝
echo.

:: ── 1) 环境检查 ──
echo  🔍 检查运行环境...
echo.
where java >nul 2>nul
if errorlevel 1 (echo  [✗] Java 未安装 & goto :err) else (echo  [✓] Java)
where mvn >nul 2>nul
if errorlevel 1 (echo  [✗] Maven 未安装 & goto :err) else (echo  [✓] Maven)
where node >nul 2>nul
if errorlevel 1 (echo  [✗] Node.js 未安装 & goto :err) else (echo  [✓] Node.js)
where npm >nul 2>nul
if errorlevel 1 (echo  [✗] npm 未安装 & goto :err) else (echo  [✓] npm)
where pnpm >nul 2>nul
if errorlevel 1 (
  echo  [!] pnpm 未安装，正在安装...
  npm install -g pnpm
)
echo  [✓] pnpm

:: ── 2) MySQL 检查 ──
echo.
echo  🗄️  检查 MySQL ...
mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% -e "SELECT 1" >nul 2>nul
if errorlevel 1 (
  echo  [!] MySQL 未运行，尝试启动...
  net start mysql >nul 2>nul
  timeout /t 3 >nul
  mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% -e "SELECT 1" >nul 2>nul
  if errorlevel 1 (echo  [✗] 无法连接 MySQL & goto :err)
)
echo  [✓] MySQL 连接正常

:: Check if db exists
for /f %%i in ('mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% --default-character-set=utf8mb4 -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%'" 2^>nul') do set TABLE_COUNT=%%i
if "%TABLE_COUNT%"=="" set TABLE_COUNT=0
if %TABLE_COUNT% LSS 5 (
  echo  [!] 数据库不存在或为空，正在导入...
  mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% --default-character-set=utf8mb4 < sql\init.sql
  mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% --default-character-set=utf8mb4 < sql\data.sql
  echo  [✓] 数据库初始化完成
) else (
  echo  [✓] 数据库已存在
)

:: ── 3) 安装依赖 ──
echo.
echo  📦 检查项目依赖...
if not exist "backend\target\classes" (
  echo  [!] 后端未编译，正在编译...
  cd backend && mvn compile -q && cd ..
)
echo  [✓] 后端已编译

if not exist "admin\node_modules" (
  echo  [!] 管理端依赖未安装...
  cd admin && pnpm install && cd ..
)
echo  [✓] 管理端依赖已安装

if not exist "frontend\node_modules" (
  echo  [!] 用户端依赖未安装...
  cd frontend && npm install && cd ..
)
echo  [✓] 用户端依赖已安装

:: ── 4) 启动服务 ──
echo.
echo  🚀 启动服务...

:: Backend (red window)
start "后端 - Spring Boot" /d "%SCRIPT_DIR%backend" cmd /k "color 4F && title 后端 Spring Boot && mvn spring-boot:run"

:: Admin (blue window)
start "管理端 - SoybeanAdmin" /d "%SCRIPT_DIR%admin" cmd /k "color 1F && title 管理端 SoybeanAdmin && pnpm dev"

:: Frontend (cyan window)
start "用户端 - Vue" /d "%SCRIPT_DIR%frontend" cmd /k "color 3F && title 用户端 Vue && npx vite --port %FRONTEND_PORT%"

:: Wait and show info
echo.
echo  ⏳ 等待服务就绪（约30秒）...
timeout /t 30 >nul

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║  ✅ 所有服务已启动                                   ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  后端 API   →  http://localhost:%BACKEND_PORT%             ║
echo  ║  管理端     →  http://localhost:%ADMIN_PORT%             ║
echo  ║  用户端     →  http://localhost:%FRONTEND_PORT%               ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  测试账号                                            ║
echo  ║  管理员: admin / 123456                              ║
echo  ║  药剂师: pharmacist1 / 123456                        ║
echo  ╚══════════════════════════════════════════════════════╝
echo.
pause
goto :eof

:err
echo.
echo  请安装缺失的依赖后重试。
pause
exit /b 1
