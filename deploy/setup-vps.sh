#!/bin/bash
# Instalación inicial en el VPS (ejecutar como root).
# Uso: bash setup-vps.sh prod
#      bash setup-vps.sh desa
set -e

ENV="${1:-prod}"
if [ "$ENV" != "prod" ] && [ "$ENV" != "desa" ]; then
  echo "Uso: bash setup-vps.sh [prod|desa]"
  exit 1
fi

BASE="/root/admin-club/${ENV}"
PROJECT="adminclub-${ENV}"
REPO_DIR="${REPO_DIR:-/root/admin-club/repo}"

echo "==> Entorno: ${ENV}"
echo "==> Directorio: ${BASE}"

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

# Clonar repo (necesario para SQL init y plantillas)
if [ ! -d "$REPO_DIR/.git" ]; then
  echo "==> Clona el repo en ${REPO_DIR} (ajusta la URL):"
  echo "    git clone https://github.com/TU_USUARIO/TU_REPO.git ${REPO_DIR}"
  exit 1
fi

mkdir -p "${BASE}/src/main/resources/db"

cp "${REPO_DIR}/deploy/docker-compose.yml" "${BASE}/"
cp "${REPO_DIR}/deploy/${ENV}.env.example" "${BASE}/.env"
cp "${REPO_DIR}/src/main/resources/db/scriptFinal.sql" "${BASE}/src/main/resources/db/"
cp "${REPO_DIR}/src/main/resources/import.sql" "${BASE}/src/main/resources/"

echo ""
echo "==> Archivos copiados a ${BASE}"
echo "==> EDITA ${BASE}/.env antes de continuar:"
echo "    - DOCKER_IMAGE=tuusuario/mi-club:${ENV}"
echo "    - MYSQL_ROOT_PASSWORD y MYSQL_PASSWORD (genera contraseñas seguras)"
echo "    - SPRING_DATASOURCE_PASSWORD (igual que MYSQL_PASSWORD)"
echo "    - SPRING_MAIL_PASSWORD (IRCD)"
echo ""
echo "Luego ejecuta:"
echo "  cd ${BASE}"
echo "  docker compose -p ${PROJECT} up -d mysql"
echo "  # Espera ~30s a que MySQL inicie, luego el primer push a GitHub despliega la app"
echo "  docker compose -p ${PROJECT} ps"
