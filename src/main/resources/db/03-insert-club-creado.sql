-- Club creado detectado en ejecución local (2026-04-15)
-- Incluye club + usuario administrador del club (ROLE_CLUB)

INSERT INTO club (id, nombre, codigo, tipo, estado, fecha_alta, fecha_baja, logo, dia_vencimiento_cuota)
VALUES (1, 'Club-1', 'club-1', 'Futbol', '1', '2026-04-15 08:21:13', NULL, NULL, 1);

INSERT INTO usuarios (id, id_club, email, password, nombre, apellido, enabled, estado, fecha_creacion, direccion, telefono, rut)
VALUES (2, 1, 'ejperezcardenas@gmail.com',
        '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq',
        'Club-1', NULL, b'1', '1', '2026-04-15 08:21:13', NULL, NULL, NULL);

INSERT INTO usuarios_rol (id_usuario, authority)
VALUES (2, 'ROLE_CLUB');
