-- Seed generado automáticamente (ExportImportSqlTool)
-- Origen: jdbc:mysql://127.0.0.1:3306/bd_adm_club_desa?... usuario=root

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- club (1)
INSERT INTO `club` (`dia_vencimiento_cuota`, `fecha_alta`, `fecha_baja`, `id`, `codigo`, `estado`, `nombre`, `tipo`, `logo`) VALUES (1, '2026-04-15 08:21:13', NULL, 1, 'club-1', '1', 'Club-1', 'Futbol', NULL);

-- bancos (5)
INSERT INTO `bancos` (`id`, `nombre`) VALUES (1, 'Banco Estado');
INSERT INTO `bancos` (`id`, `nombre`) VALUES (2, 'BCI');
INSERT INTO `bancos` (`id`, `nombre`) VALUES (3, 'Banco Falabella');
INSERT INTO `bancos` (`id`, `nombre`) VALUES (4, 'Banco Chile');
INSERT INTO `bancos` (`id`, `nombre`) VALUES (5, 'Banco Itau');

-- usuarios (3)
INSERT INTO `usuarios` (`enabled`, `fecha_creacion`, `id`, `id_club`, `apellido`, `direccion`, `email`, `estado`, `nombre`, `password`, `rut`, `telefono`) VALUES (b'1', NULL, 1, NULL, 'Global', NULL, 'estebanjpc@gmail.com', '1', 'Admin', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);
INSERT INTO `usuarios` (`enabled`, `fecha_creacion`, `id`, `id_club`, `apellido`, `direccion`, `email`, `estado`, `nombre`, `password`, `rut`, `telefono`) VALUES (b'1', '2026-04-15 08:21:13', 2, 1, NULL, NULL, 'ejperezcardenas@gmail.com', '1', 'Club-1', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);
INSERT INTO `usuarios` (`enabled`, `fecha_creacion`, `id`, `id_club`, `apellido`, `direccion`, `email`, `estado`, `nombre`, `password`, `rut`, `telefono`) VALUES (b'1', '2026-04-15 10:31:15', 3, 1, 'Perez', 'Santa Ana 1264', 'ircd.chile@gmail.com', '1', 'Esteban', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', '1777371', '997249492');

-- categorias (2)
INSERT INTO `categorias` (`valor_cuota`, `id`, `id_club`, `nombre`) VALUES (12000, 1, 1, 'Infantil');
INSERT INTO `categorias` (`valor_cuota`, `id`, `id_club`, `nombre`) VALUES (15000, 2, 1, 'Juvenil');

-- categoria_valor_vigencia (2)
INSERT INTO `categoria_valor_vigencia` (`anio`, `mes`, `valor_cuota`, `id`, `id_categoria`) VALUES (2026, 1, 12000, 1, 1);
INSERT INTO `categoria_valor_vigencia` (`anio`, `mes`, `valor_cuota`, `id`, `id_categoria`) VALUES (2026, 1, 15000, 2, 2);

-- usuarios_rol (3)
INSERT INTO `usuarios_rol` (`id`, `id_usuario`, `authority`) VALUES (1, 1, 'ROLE_ADMIN');
INSERT INTO `usuarios_rol` (`id`, `id_usuario`, `authority`) VALUES (2, 2, 'ROLE_CLUB');
INSERT INTO `usuarios_rol` (`id`, `id_usuario`, `authority`) VALUES (3, 3, 'ROLE_USER');

-- cuentas_bancarias (1)
INSERT INTO `cuentas_bancarias` (`id`, `id_banco`, `id_club`, `khipu_api_key`, `khipu_merchant_secret`, `khipu_api_url`, `email`, `nombre_titular`, `numero_cuenta`, `rut`, `tipo_cuenta`) VALUES (1, 1, 1, NULL, NULL, NULL, 'ejperezcardenas@gmail.com', 'Datos', '123123213', '12312', 'Cuenta Corriente');

-- deportistas (2)
INSERT INTO `deportistas` (`fecha_ingreso`, `fecha_nacimiento`, `id`, `id_categoria`, `id_usuario`, `apellido`, `estado`, `nombre`, `rut`, `sexo`) VALUES ('2026-01-01', '1986-01-31', 1, 2, 3, 'Perez', '1', 'Esteban', '1777371', 'M');
INSERT INTO `deportistas` (`fecha_ingreso`, `fecha_nacimiento`, `id`, `id_categoria`, `id_usuario`, `apellido`, `estado`, `nombre`, `rut`, `sexo`) VALUES ('2026-02-01', '1995-12-24', 2, 1, 3, 'Marin', '1', 'Judith', '12121', 'F');

SET FOREIGN_KEY_CHECKS=1;
