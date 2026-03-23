#!/bin/bash
# ╔══════════════════════════════════════════════════════╗
# ║   💊 药店药品信息管理系统 — Docker 一键启动 (Mac/Linux)  ║
# ╚══════════════════════════════════════════════════════╝
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'
YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║${BOLD}   💊 药店药品信息管理系统 — Docker 启动               ${NC}${GREEN}║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
echo ""

# ── 检查 Docker ──
if ! command -v docker &>/dev/null; then
  echo -e "${RED}[✗]${NC} Docker 未安装，请先安装 Docker Desktop"
  exit 1
fi

if ! docker info &>/dev/null 2>&1; then
  echo -e "${RED}[✗]${NC} Docker 未运行，请先启动 Docker Desktop"
  exit 1
fi
echo -e "${GREEN}[✓]${NC} Docker 运行正常"

# ── 启动容器 ──
echo ""
echo -e "${BOLD}🚀 启动 Docker 容器...${NC}"
echo ""
docker compose up -d

# ── 等待服务就绪 ──
echo ""
echo -e "${BOLD}⏳ 等待服务就绪...${NC}"

# 等待后端就绪
for i in $(seq 1 90); do
  if curl -s http://localhost:8080 >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

# 等待前端就绪
for i in $(seq 1 30); do
  if curl -s http://localhost:3000 >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

# 等待管理端就绪
for i in $(seq 1 30); do
  if curl -s http://localhost:9527 >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

sleep 2

# ── 打印信息 ──
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║${BOLD}  ✅ 所有服务已启动                                          ${NC}${GREEN}║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}                                                              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${BOLD}📡 服务地址${NC}                                                ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${RED}后端 API${NC}     →  http://localhost:8080                       ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${CYAN}用户端${NC}       →  http://localhost:3000                       ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${BLUE}管理端${NC}       →  http://localhost:9527                       ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}                                                              ${GREEN}║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}                                                              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${BOLD}👤 测试账号${NC}                                                ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ┌────────────┬──────────────────┬──────────────┐              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  │ ${BOLD}角色${NC}       │ ${BOLD}用户名${NC}           │ ${BOLD}密码${NC}         │              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ├────────────┼──────────────────┼──────────────┤              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  │ ${RED}管理员${NC}     │ admin            │ 123456       │              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  │ ${BLUE}药剂师${NC}     │ pharmacist1      │ 123456       │              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  │ ${CYAN}仓库管理员${NC} │ warehouse1       │ 123456       │              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  └────────────┴──────────────────┴──────────────┘              ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}                                                              ${GREEN}║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}  停止服务: ${BOLD}docker compose down${NC}                                ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  查看日志: ${BOLD}docker compose logs -f${NC}                             ${GREEN}║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
