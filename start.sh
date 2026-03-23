#!/bin/bash
# ╔══════════════════════════════════════════════╗
# ║   药店药品信息管理系统 — 一键启动脚本 (Mac/Linux)   ║
# ╚══════════════════════════════════════════════╝
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ── 配置 ──
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="pharmacy_db"
DB_USER="root"
DB_PASS="ab123168"
BACKEND_PORT=8080
ADMIN_PORT=9527
FRONTEND_PORT=3000
LOG_DIR="$SCRIPT_DIR/.logs"

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'

banner() {
  echo ""
  echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
  echo -e "${GREEN}║${BOLD}   💊 药店药品信息管理系统                       ${NC}${GREEN}║${NC}"
  echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
  echo ""
}

log()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[✗]${NC} $1"; }

# ── 1) 环境检查 ──
check_env() {
  echo -e "\n${BOLD}🔍 检查运行环境...${NC}\n"
  local ok=true
  for cmd in java mvn node npm mysql; do
    if command -v $cmd &>/dev/null; then
      log "$cmd  →  $(${cmd} --version 2>&1 | head -1)"
    else
      err "$cmd 未安装"
      ok=false
    fi
  done

  if ! command -v pnpm &>/dev/null; then
    warn "pnpm 未安装，正在通过 npm 安装..."
    npm install -g pnpm
    log "pnpm 安装完成"
  else
    log "pnpm  →  $(pnpm --version)"
  fi

  if [ "$ok" = false ]; then
    err "请先安装缺失的依赖后重试"
    exit 1
  fi
}

# ── 2) MySQL 检查 ──
check_mysql() {
  echo -e "\n${BOLD}🗄️  检查 MySQL 数据库...${NC}\n"

  if ! mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" -e "SELECT 1" &>/dev/null; then
    warn "MySQL 服务未运行，尝试启动..."
    if [[ "$(uname)" == "Darwin" ]]; then
      brew services start mysql 2>/dev/null || true
    else
      sudo systemctl start mysql 2>/dev/null || sudo systemctl start mysqld 2>/dev/null || true
    fi
    sleep 3
    if ! mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" -e "SELECT 1" &>/dev/null; then
      err "无法连接 MySQL，请手动启动后重试"
      exit 1
    fi
  fi
  log "MySQL 连接正常"

  TABLE_COUNT=$(mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" --default-character-set=utf8mb4 -N -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME'" 2>/dev/null || echo "0")

  if [ "$TABLE_COUNT" -lt 5 ] 2>/dev/null; then
    warn "数据库不存在或为空，正在导入..."
    mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" --default-character-set=utf8mb4 < sql/init.sql
    mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" --default-character-set=utf8mb4 < sql/data.sql
    log "数据库初始化完成"
  else
    log "数据库已存在（$TABLE_COUNT 张表）"
  fi
}

# ── 3) 安装依赖 ──
install_deps() {
  echo -e "\n${BOLD}📦 检查项目依赖...${NC}\n"
  if [ ! -d "backend/target/classes" ]; then
    warn "后端未编译，正在编译..."
    (cd backend && mvn compile -q) || { err "后端编译失败"; exit 1; }
    log "后端编译完成"
  else
    log "后端已编译"
  fi

  if [ ! -d "admin/node_modules" ]; then
    warn "管理端依赖未安装，正在安装..."
    (cd admin && pnpm install) || { err "管理端依赖安装失败"; exit 1; }
    log "管理端依赖安装完成"
  else
    log "管理端依赖已安装"
  fi

  if [ ! -d "frontend/node_modules" ]; then
    warn "用户端依赖未安装，正在安装..."
    (cd frontend && npm install) || { err "用户端依赖安装失败"; exit 1; }
    log "用户端依赖安装完成"
  else
    log "用户端依赖已安装"
  fi
}

# ── 4) 端口检查 ──
check_ports() {
  echo -e "\n${BOLD}🔌 检查端口...${NC}\n"
  for port in $BACKEND_PORT $ADMIN_PORT $FRONTEND_PORT; do
    if lsof -i :$port &>/dev/null; then
      warn "端口 $port 被占用"
      read -p "  是否终止占用进程？(y/n) " yn
      if [ "$yn" = "y" ]; then
        lsof -ti :$port | xargs kill -9 2>/dev/null || true
        log "端口 $port 已释放"
      fi
    else
      log "端口 $port 可用"
    fi
  done
}

# ── 5) 启动服务 ──
start_services() {
  echo -e "\n${BOLD}🚀 启动服务...${NC}\n"
  mkdir -p "$LOG_DIR"

  # Backend
  echo -e "  ${RED}[后端]${NC} 启动 Spring Boot (端口 $BACKEND_PORT)..."
  (cd backend && mvn spring-boot:run -q > "$LOG_DIR/backend.log" 2>&1) &
  echo $! > "$LOG_DIR/backend.pid"

  # Admin
  echo -e "  ${BLUE}[管理端]${NC} 启动 SoybeanAdmin (端口 $ADMIN_PORT)..."
  (cd admin && pnpm dev > "$LOG_DIR/admin.log" 2>&1) &
  echo $! > "$LOG_DIR/admin.pid"

  # Frontend
  echo -e "  ${CYAN}[用户端]${NC} 启动前端 (端口 $FRONTEND_PORT)..."
  (cd frontend && npx vite --port $FRONTEND_PORT > "$LOG_DIR/frontend.log" 2>&1) &
  echo $! > "$LOG_DIR/frontend.pid"

  # Wait for ports to be ready
  echo -e "\n${BOLD}⏳ 等待服务就绪...${NC}"
  for port in $BACKEND_PORT $ADMIN_PORT $FRONTEND_PORT; do
    for i in $(seq 1 60); do
      if lsof -i :$port &>/dev/null; then break; fi
      sleep 1
    done
  done
  sleep 2

  echo ""
  echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
  echo -e "${GREEN}║${BOLD}  ✅ 所有服务已启动                                    ${NC}${GREEN}║${NC}"
  echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
  echo -e "${GREEN}║${NC}  ${RED}后端 API${NC}   →  http://localhost:${BACKEND_PORT}            ${GREEN}║${NC}"
  echo -e "${GREEN}║${NC}  ${BLUE}管理端${NC}     →  http://localhost:${ADMIN_PORT}            ${GREEN}║${NC}"
  echo -e "${GREEN}║${NC}  ${CYAN}用户端${NC}     →  http://localhost:${FRONTEND_PORT}              ${GREEN}║${NC}"
  echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
  echo -e "${GREEN}║${NC}  ${BOLD}测试账号${NC}                                             ${GREEN}║${NC}"
  echo -e "${GREEN}║${NC}  管理员: admin / 123456                               ${GREEN}║${NC}"
  echo -e "${GREEN}║${NC}  药剂师: pharmacist1 / 123456                         ${GREEN}║${NC}"
  echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
  echo -e "${GREEN}║${NC}  按 ${BOLD}Ctrl+C${NC} 停止所有服务                                ${GREEN}║${NC}"
  echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
  echo ""

  # Tail all logs with colored prefixes
  tail -f "$LOG_DIR/backend.log" 2>/dev/null | sed "s/^/$(printf '\033[0;31m')[后端]$(printf '\033[0m') /" &
  TAIL1=$!
  tail -f "$LOG_DIR/admin.log" 2>/dev/null | sed "s/^/$(printf '\033[0;34m')[管理端]$(printf '\033[0m') /" &
  TAIL2=$!
  tail -f "$LOG_DIR/frontend.log" 2>/dev/null | sed "s/^/$(printf '\033[0;36m')[用户端]$(printf '\033[0m') /" &
  TAIL3=$!

  # Trap Ctrl+C
  trap "echo ''; echo '正在停止所有服务...'; bash '$SCRIPT_DIR/stop.sh'; exit 0" INT
  wait
}

# ── Main ──
banner
check_env
check_mysql
install_deps
check_ports
start_services
