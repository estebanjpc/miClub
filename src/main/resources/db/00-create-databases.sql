-- Crea las bases por ambiente en el MySQL compartido (mysql-server).
-- Se ejecuta automáticamente al inicializar el contenedor por primera vez.

CREATE DATABASE IF NOT EXISTS bd_adm_club_pro
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bd_adm_club_desa
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
