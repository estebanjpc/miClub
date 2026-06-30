#!/bin/bash
# Ejecutado solo en la primera inicialización del volumen MySQL.
set -e

SCHEMA="/docker-entrypoint-initdb.d/schema/scriptFinal.sql"

for db in bd_adm_club_pro bd_adm_club_desa; do
  echo "==> Aplicando esquema en ${db}..."
  mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${db}" < "${SCHEMA}"
done

echo "==> Esquemas bd_adm_club_pro y bd_adm_club_desa listos."
