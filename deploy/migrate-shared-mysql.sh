#!/bin/bash
# Migra datos desde los contenedores MySQL antiguos (mysql-club-prod / mysql-club-desa)
# hacia mysql-server con bd_adm_club_pro y bd_adm_club_desa.
#
# Ejecutar en el VPS como root, ANTES de apagar los contenedores viejos.
set -e

ROOT_PASS="${MYSQL_ROOT_PASSWORD:-Mysql.mysql}"
BACKUP_DIR="${BACKUP_DIR:-/root/admin-club/backups/mysql-migracion-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "${BACKUP_DIR}"

echo "==> Backups en ${BACKUP_DIR}"

if docker ps -a --format '{{.Names}}' | grep -q '^mysql-club-prod$'; then
  echo "    Dump PROD (bd_adm_club)..."
  docker exec mysql-club-prod mysqldump -uroot -p"${ROOT_PASS}" --databases bd_adm_club \
    > "${BACKUP_DIR}/bd_adm_club_prod.sql"
else
  echo "    mysql-club-prod no encontrado, se omite backup PROD"
fi

if docker ps -a --format '{{.Names}}' | grep -q '^mysql-club-desa$'; then
  echo "    Dump DESA (bd_adm_club)..."
  docker exec mysql-club-desa mysqldump -uroot -p"${ROOT_PASS}" --databases bd_adm_club \
    > "${BACKUP_DIR}/bd_adm_club_desa.sql"
else
  echo "    mysql-club-desa no encontrado, se omite backup DESA"
fi

echo "==> Levantando mysql-server (si no está arriba)..."
cd /root/mysql
docker compose up -d
echo "    Esperando MySQL..."
sleep 25

import_dump() {
  local file="$1"
  local target_db="$2"
  if [ ! -f "$file" ]; then
    return 0
  fi
  echo "==> Importando ${file} → ${target_db}"
  # Reemplaza nombre de BD del dump antiguo por el nuevo
  sed 's/`bd_adm_club`/`'"${target_db}"'`/g; s/USE `bd_adm_club`/USE `'"${target_db}"'`/g' "${file}" \
    | docker exec -i mysql-server mysql -uroot -p"${ROOT_PASS}"
}

import_dump "${BACKUP_DIR}/bd_adm_club_prod.sql" "bd_adm_club_pro"
import_dump "${BACKUP_DIR}/bd_adm_club_desa.sql" "bd_adm_club_desa"

echo ""
echo "==> Migración completada."
echo "    1. Actualiza docker-compose de prod y desa (sin servicio db-*)"
echo "    2. docker compose up -d en prod y desa"
echo "    3. Verifica apps y luego detén contenedores viejos:"
echo "       docker stop mysql-club-prod mysql-club-desa"
echo "    Backups guardados en: ${BACKUP_DIR}"
