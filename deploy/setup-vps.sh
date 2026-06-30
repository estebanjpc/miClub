#!/bin/bash
# Instalación inicial en el VPS (ejecutar como root).
#
# Uso:
#   bash setup-vps.sh mysql    # una sola vez: MySQL compartido
#   bash setup-vps.sh prod     # app producción
#   bash setup-vps.sh desa     # app desarrollo
set -e

ENV="${1:-}"
if [ "$ENV" != "mysql" ] && [ "$ENV" != "prod" ] && [ "$ENV" != "desa" ]; then
  echo "Uso: bash setup-vps.sh [mysql|prod|desa]"
  exit 1
fi

REPO_DIR="${REPO_DIR:-/root/admin-club/repo}"

if [ ! -d "$REPO_DIR/.git" ]; then
  echo "==> Clona el repo en ${REPO_DIR} (ajusta la URL):"
  echo "    git clone https://github.com/TU_USUARIO/TU_REPO.git ${REPO_DIR}"
  exit 1
fi

# Docker
if ! command -v docker >/dev/null 2>&1; then
  echo "==> Instalando Docker..."
  apt-get update
  apt-get install -y ca-certificates curl git
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null
  apt-get update
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  systemctl enable docker
  systemctl start docker
else
  echo "==> Docker ya instalado"
fi

if [ "$ENV" = "mysql" ]; then
  BASE="/root/mysql"
  mkdir -p "${BASE}/init/schema"
  cp "${REPO_DIR}/deploy/docker-compose-mysql.yml" "${BASE}/docker-compose.yml"
  cp "${REPO_DIR}/src/main/resources/db/00-create-databases.sql" "${BASE}/init/"
  cp "${REPO_DIR}/deploy/init-schemas.sh" "${BASE}/init/01-init-schemas.sh"
  chmod +x "${BASE}/init/01-init-schemas.sh"
  cp "${REPO_DIR}/src/main/resources/db/scriptFinal.sql" "${BASE}/init/schema/"
  echo ""
  echo "==> MySQL compartido listo en ${BASE}"
  echo "    cd ${BASE} && docker compose up -d"
  echo "    Contenedor: mysql-server | Red: shared-db-net"
  exit 0
fi

BASE="/root/admin-club/${ENV}"
PROJECT="adminclub-${ENV}"

cp "${REPO_DIR}/deploy/docker-compose-${ENV}.yml" "${BASE}/docker-compose.yml"
cp "${REPO_DIR}/deploy/${ENV}.env.example" "${BASE}/.env"

echo ""
echo "==> Entorno ${ENV} copiado a ${BASE}"
echo "==> EDITA ${BASE}/.env (SPRING_DATASOURCE_*, SPRING_MAIL_PASSWORD, DOCKER_IMAGE)"
echo ""
echo "Requisito: MySQL compartido ya levantado (bash setup-vps.sh mysql)"
echo "Luego:"
echo "  cd ${BASE}"
echo "  docker compose -p ${PROJECT} up -d"
echo "  docker compose -p ${PROJECT} ps"
