-- AdminClub - Esquema base MySQL 8+
-- Copia de referencia local; fuente canónica: scriptFinal.sql
--
-- USO:
--   Instalación nueva: scriptFinal.sql (o este archivo, contenido equivalente).
--   Base existente:    04-upgrade-existing-db.sql

SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS club (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  codigo VARCHAR(255) NOT NULL,
  tipo VARCHAR(255) NOT NULL,
  estado VARCHAR(255) NOT NULL,
  fecha_alta DATETIME NULL,
  fecha_baja DATETIME NULL,
  logo LONGBLOB NULL,
  dia_vencimiento_cuota INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_club_nombre (nombre),
  UNIQUE KEY uk_club_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bancos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_club BIGINT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NULL,
  nombre VARCHAR(255) NOT NULL,
  apellido VARCHAR(255) NULL,
  enabled BIT(1) NULL,
  estado VARCHAR(255) NOT NULL,
  fecha_creacion DATETIME NULL,
  direccion VARCHAR(255) NULL,
  telefono VARCHAR(255) NULL,
  rut VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_email_club (email, id_club),
  KEY idx_usuarios_club (id_club),
  CONSTRAINT fk_usuarios_club FOREIGN KEY (id_club) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuarios_rol (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT NULL,
  authority VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_rol_usuario_authority (id_usuario, authority),
  KEY idx_usuarios_rol_usuario (id_usuario),
  CONSTRAINT fk_usuarios_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cuentas_bancarias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre_titular VARCHAR(255) NULL,
  rut VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  id_banco BIGINT NULL,
  tipo_cuenta VARCHAR(255) NULL,
  numero_cuenta VARCHAR(255) NULL,
  id_club BIGINT NULL,
  khipu_api_url VARCHAR(500) NULL,
  khipu_api_key VARCHAR(256) NULL,
  khipu_merchant_secret VARCHAR(256) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cuentas_bancarias_club (id_club),
  KEY idx_cuentas_bancarias_banco (id_banco),
  CONSTRAINT fk_cuentas_bancarias_banco FOREIGN KEY (id_banco) REFERENCES bancos(id),
  CONSTRAINT fk_cuentas_bancarias_club FOREIGN KEY (id_club) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categorias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NULL,
  valor_cuota INT NOT NULL,
  id_club BIGINT NULL,
  PRIMARY KEY (id),
  KEY idx_categorias_club (id_club),
  CONSTRAINT fk_categorias_club FOREIGN KEY (id_club) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categoria_valor_vigencia (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_categoria BIGINT NOT NULL,
  anio INT NOT NULL,
  mes INT NOT NULL,
  valor_cuota INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categoria_valor_vigencia_periodo (id_categoria, anio, mes),
  KEY idx_categoria_valor_vigencia_categoria (id_categoria),
  CONSTRAINT fk_categoria_valor_vigencia_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deportistas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NULL,
  apellido VARCHAR(255) NULL,
  rut VARCHAR(255) NOT NULL,
  fecha_nacimiento DATE NULL,
  sexo VARCHAR(255) NULL,
  id_categoria BIGINT NULL,
  estado VARCHAR(255) NULL,
  id_usuario BIGINT NULL,
  fecha_ingreso DATE NULL,
  PRIMARY KEY (id),
  KEY idx_deportistas_categoria (id_categoria),
  KEY idx_deportistas_usuario (id_usuario),
  CONSTRAINT fk_deportistas_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id),
  CONSTRAINT fk_deportistas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS temporadas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_club BIGINT NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  activa BIT(1) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_temporadas_club (id_club),
  CONSTRAINT fk_temporadas_club FOREIGN KEY (id_club) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orden_pago (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT NULL,
  monto_total INT NULL,
  fecha_creacion DATETIME NULL,
  medio_pago VARCHAR(255) NULL,
  khipu_payment_id VARCHAR(255) NULL,
  khipu_url VARCHAR(255) NULL,
  estado VARCHAR(255) NULL,
  fecha_pago DATETIME NULL,
  khipu_transaction_id VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_orden_pago_usuario (id_usuario),
  KEY idx_orden_pago_khipu_payment_id (khipu_payment_id),
  CONSTRAINT fk_orden_pago_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NULL,
  deportista_id BIGINT NULL,
  fecha DATETIME NULL,
  mes INT NULL,
  anio INT NULL,
  estado VARCHAR(255) NULL,
  medio_pago VARCHAR(255) NULL,
  monto INT NULL,
  concepto VARCHAR(255) NULL,
  id_temporada BIGINT NULL,
  observacion VARCHAR(255) NULL,
  comprobante_transferencia MEDIUMBLOB NULL,
  comprobante_content_type VARCHAR(120) NULL,
  comprobante_nombre_archivo VARCHAR(255) NULL,
  orden_pago_id BIGINT NULL,
  PRIMARY KEY (id),
  KEY idx_pago_club (club_id),
  KEY idx_pago_deportista (deportista_id),
  KEY idx_pago_temporada (id_temporada),
  KEY idx_pago_orden (orden_pago_id),
  CONSTRAINT fk_pago_club FOREIGN KEY (club_id) REFERENCES club(id),
  CONSTRAINT fk_pago_deportista FOREIGN KEY (deportista_id) REFERENCES deportistas(id),
  CONSTRAINT fk_pago_temporada FOREIGN KEY (id_temporada) REFERENCES temporadas(id),
  CONSTRAINT fk_pago_orden FOREIGN KEY (orden_pago_id) REFERENCES orden_pago(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS club_historial_cambio (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  fecha DATETIME NOT NULL,
  tipo_cambio VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_club_historial_cambio_club (club_id),
  KEY idx_club_historial_cambio_usuario (usuario_id),
  CONSTRAINT fk_club_historial_cambio_club FOREIGN KEY (club_id) REFERENCES club(id),
  CONSTRAINT fk_club_historial_cambio_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  type VARCHAR(40) NOT NULL,
  enabled BIT(1) NOT NULL,
  days_offset INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_config_club_type (club_id, type),
  KEY idx_notification_config_club (club_id),
  CONSTRAINT fk_notification_config_club FOREIGN KEY (club_id) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_send_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  deportista_id BIGINT NOT NULL,
  notification_type VARCHAR(40) NOT NULL,
  mes INT NOT NULL,
  anio INT NOT NULL,
  sent_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_log_dedupe (club_id, deportista_id, notification_type, mes, anio),
  KEY idx_notification_log_club (club_id),
  CONSTRAINT fk_notification_log_club FOREIGN KEY (club_id) REFERENCES club(id),
  CONSTRAINT fk_notification_log_deportista FOREIGN KEY (deportista_id) REFERENCES deportistas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS email_envios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_club BIGINT NOT NULL,
  tipo VARCHAR(80) NOT NULL,
  asunto VARCHAR(180) NOT NULL,
  mensaje TEXT NOT NULL,
  filtros_aplicados VARCHAR(500) NULL,
  total_destinatarios INT NOT NULL,
  fecha_envio DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_email_envios_club (id_club),
  CONSTRAINT fk_email_envios_club FOREIGN KEY (id_club) REFERENCES club(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asistencia_clase (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  deportista_id BIGINT NOT NULL,
  entrenador_id BIGINT NULL,
  fecha_clase DATE NOT NULL,
  presente BIT(1) NOT NULL,
  observacion VARCHAR(255) NULL,
  fecha_registro DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_asistencia_clase_unica (club_id, deportista_id, fecha_clase),
  KEY idx_asistencia_clase_club (club_id),
  KEY idx_asistencia_clase_deportista (deportista_id),
  KEY idx_asistencia_clase_entrenador (entrenador_id),
  CONSTRAINT fk_asistencia_clase_club FOREIGN KEY (club_id) REFERENCES club(id),
  CONSTRAINT fk_asistencia_clase_deportista FOREIGN KEY (deportista_id) REFERENCES deportistas(id),
  CONSTRAINT fk_asistencia_clase_entrenador FOREIGN KEY (entrenador_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS no_pago_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  mes INT NOT NULL,
  anio INT NOT NULL,
  scope VARCHAR(30) NOT NULL,
  categoria_id BIGINT NULL,
  deportista_id BIGINT NULL,
  observacion VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_no_pago_club_periodo (club_id, anio, mes),
  KEY idx_no_pago_categoria (categoria_id),
  KEY idx_no_pago_deportista (deportista_id),
  CONSTRAINT fk_no_pago_club FOREIGN KEY (club_id) REFERENCES club(id),
  CONSTRAINT fk_no_pago_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
  CONSTRAINT fk_no_pago_deportista FOREIGN KEY (deportista_id) REFERENCES deportistas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago_comprobante (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_Pago BIGINT NULL,
  file_blob LONGBLOB NULL COMMENT 'Archivo adjunto; LONGBLOB alineado con PagoComprobante JPA',
  PRIMARY KEY (id),
  KEY idx_pago_comprobante_pago (id_Pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
