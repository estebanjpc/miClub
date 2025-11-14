INSERT INTO club (id, fecha_alta, codigo, nombre, tipo, logo, estado) VALUES (1, '2025-11-10 16:58:42', 'club-1', 'Club-1', 'Baquetball', NULL,1);
INSERT INTO club (id, fecha_alta, codigo, nombre, tipo, logo,estado) VALUES (2, '2025-11-10 16:58:42','club-2', 'Club-2', 'Futbol', NULL,1);


INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, NULL, 1, NULL, 'Global', NULL,'estebanjpc@gmail.com', '1', 'Admin', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);
INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, NULL, 2, 1, 'AP', NULL, 'club1@gmail.com', '1', 'Club-1', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);
INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, NULL, 3, 2, 'AP', NULL,'club2@gmail.com', '1', 'Club-2', '$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq', NULL, NULL);
INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, '2025-11-13 16:43:14', 4, 1, 'Perez', 'Santa Ana 1264, San Ramon', 'ejperezcardenas@gmail.com', '1', 'Esteban', '$2a$10$RZzATB6V1ozjIDVYk.3oOO628Yx2.TzlD4bG8RRO11hoxWjMwkX.a', '16074474K', '997249492');
INSERT INTO usuarios (enabled, fecha_creacion, id, id_club, apellido, direccion, email,estado,nombre,password,rut,telefono) VALUES (1, '2025-11-13 16:46:48', 5, 2, 'Perez', 'Santa Ana 1264, San Ramon', 'ejperezcardenas@gmail.com', '0', 'Esteban', '$2a$10$FyaxJdN5Qaids2alWtSEOeo1iJFtvreZQOchWoDFMV/AFKz5k7/b.', '16074474K', '997249492');

INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (1, 1, 'ROLE_ADMIN');
INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (2, 2, 'ROLE_CLUB');
INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (3, 3, 'ROLE_CLUB');
INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (4, 4, 'ROLE_USER');
INSERT INTO usuarios_rol (id, id_usuario, authority) VALUES (5, 5, 'ROLE_USER');

INSERT INTO bancos (id, nombre) VALUES (1, 'Banco Estado');
INSERT INTO bancos (id, nombre) VALUES (2, 'BCI');
INSERT INTO bancos (id, nombre) VALUES (3, 'Banco Falabella');
INSERT INTO bancos (id, nombre) VALUES (4, 'Banco Chile');
INSERT INTO bancos (id, nombre) VALUES (5, 'Banco Itau');

INSERT INTO categorias (id,id_club, nombre,valor_cuota) VALUES (1, 1, 'Juvenil',15000);
INSERT INTO categorias (id,id_club, nombre,valor_cuota) VALUES (2, 2, 'Juvenil',15000);

INSERT INTO deportistas (fecha_nacimiento, id, id_categoria, id_usuario,apellido, nombre, rut, sexo,estado) VALUES ('1986-01-31', 1, 1,4, 'Perez','Esteban', '16074474K', 'M',1);
INSERT INTO deportistas (fecha_nacimiento, id, id_categoria, id_usuario,apellido, nombre, rut, sexo,estado) VALUES ('1986-01-31', 2, 2,5, 'Perez','Esteban', '16074474K', 'M',1);

INSERT INTO cuentas_bancarias (id,id_banco,id_club,email,nombre_titular,numero_cuenta,rut,tipo_cuenta) VALUES (1,1,1,'email@email.com','Titular-1',1231,'16074474-K','Cta Cte');
INSERT INTO cuentas_bancarias (id,id_banco,id_club,email,nombre_titular,numero_cuenta,rut,tipo_cuenta) VALUES (2,2,2,'email@email.com','Titular-1',1231,'16074474-K','Cta Cte');