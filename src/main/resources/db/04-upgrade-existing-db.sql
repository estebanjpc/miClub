-- AdminClub - Migraciones para bases de datos EXISTENTES
-- MySQL 8+
--
-- Cuándo ejecutar:
--   - El arranque falla con Schema-validation y tipos BLOB incorrectos.
--   - La BD se creó con Hibernate antiguo (TINYBLOB/BLOB) antes de scriptFinal.sql.
--   - Actualizas DESA/PROD sin recrear el volumen de MySQL.
--
-- Cuándo NO ejecutar:
--   - Instalación nueva: usa scriptFinal.sql en BD vacía (ya incluye los tipos correctos).
--
-- Ejecución (ejemplo DESA):
--   docker exec -i mysql-club-desa mysql -uroot -p bd_adm_club < 04-upgrade-existing-db.sql
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
