#!/bin/bash
# ╔══════════════════════════════════════════════╗
# ║   药店药品信息管理系统 — 一键停止脚本 (Mac/Linux)   ║
# ╚══════════════════════════════════════════════╝
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$SCRIPT_DIR/.logs"

RED='\033[0;31m'; GREEN='\033[0;32m'; NC='\033[0m'; BOLD='\033[1m'

echo ""
echo -e "${RED}${BOLD}🛑 停止所有服务...${NC}"
echo ""

# Kill by PID files
for pidfile in "$LOG_DIR"/*.pid; do
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    name=$(basename "$pidfile" .pid)
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null
      echo -e "${GREEN}[✓]${NC} 已停止 $name (PID: $pid)"
    fi
    rm -f "$pidfile"
  fi
done

# Kill by ports (fallback)
for port in 8080 9527 3000; do
  pids=$(lsof -ti :$port 2>/dev/null)
  if [ -n "$pids" ]; then
    echo "$pids" | xargs kill -9 2>/dev/null
    echo -e "${GREEN}[✓]${NC} 已释放端口 $port"
  fi
done

# Kill tail processes
pkill -f "tail -f $LOG_DIR" 2>/dev/null

echo ""
echo -e "${GREEN}${BOLD}✅ 所有服务已停止${NC}"
echo ""
