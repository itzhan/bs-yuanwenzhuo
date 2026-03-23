@echo off
chcp 65001 >nul 2>nul
title 药店药品信息管理系统 — 停止
echo.
echo  🛑 停止所有服务...
echo.

:: Kill by port
for %%p in (8080 9527 3000) do (
  for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%%p " ^| findstr "LISTENING" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>nul
    echo  [✓] 已停止端口 %%p 上的进程 ^(PID: %%a^)
  )
)

:: Close named windows
taskkill /FI "WINDOWTITLE eq 后端*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq 管理端*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq 用户端*" /F >nul 2>nul

echo.
echo  ✅ 所有服务已停止
echo.
pause
