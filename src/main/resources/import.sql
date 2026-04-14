
INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, NULL, 1, NULL, 'Global', NULL,'estebanjpc@gmail.com', '1', 'Admin', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);

INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (1, 1, 'ROLE_ADMIN');

INSERT INTO bancos (id, nombre) VALUES (1, 'Banco Estado');
INSERT INTO bancos (id, nombre) VALUES (2, 'BCI');
INSERT INTO bancos (id, nombre) VALUES (3, 'Banco Falabella');
INSERT INTO bancos (id, nombre) VALUES (4, 'Banco Chile');
INSERT INTO bancos (id, nombre) VALUES (5, 'Banco Itau');
