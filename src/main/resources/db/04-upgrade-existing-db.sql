-- AdminClub - Migraciones para bases de datos EXISTENTES
-- MySQL 8+
--
-- Ejecutar en la BD del ambiente correspondiente:
--   PROD → USE bd_adm_club_pro;
--   DESA → USE bd_adm_club_desa;
--
-- Cuándo ejecutar:
--   - El arranque falla con Schema-validation y tipos BLOB incorrectos.
--   - La BD se creó con Hibernate antiguo (TINYBLOB/BLOB).
--
-- Cuándo NO ejecutar:
--   - Instalación nueva con scriptFinal.sql (ya incluye los tipos correctos).
--
-- Ejemplo CLI:
--   docker exec -i mysql-server mysql -uroot -p bd_adm_club_pro < 04-upgrade-existing-db.sql
--
-- Idempotente: repetir los ALTER no da error si la columna ya tiene el tipo correcto.

SET NAMES utf8mb4;

-- Logo del club (Club.logo)
ALTER TABLE club
  MODIFY COLUMN logo LONGBLOB NULL;

-- Comprobante de transferencia en pagos (Pago.comprobanteTransferencia)
ALTER TABLE pago
  MODIFY COLUMN comprobante_transferencia MEDIUMBLOB NULL;

-- Tabla legacy de comprobantes (PagoComprobante.fileBlob)
ALTER TABLE pago_comprobante
  MODIFY COLUMN file_blob LONGBLOB NULL;
