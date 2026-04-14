-- AdminClub - Datos base mínimos
-- Ajusta email/password antes de usar en ambientes reales.

INSERT INTO bancos (id, nombre) VALUES
  (1, 'Banco Estado'),
  (2, 'BCI'),
  (3, 'Banco Falabella'),
  (4, 'Banco Chile'),
  (5, 'Banco Itau')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO usuarios (
  id, enabled, fecha_creacion, id_club, apellido, direccion, email, estado, nombre, password, rut, telefono
) VALUES (
  1, 1, NULL, NULL, 'Global', NULL, 'estebanjpc@gmail.com', '1', 'Admin',
  '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq',
  NULL, NULL
)
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nombre = VALUES(nombre),
  apellido = VALUES(apellido),
  password = VALUES(password),
  estado = VALUES(estado),
  enabled = VALUES(enabled);

INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES
  (1, 1, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE
  id_usuario = VALUES(id_usuario),
  authority = VALUES(authority);
