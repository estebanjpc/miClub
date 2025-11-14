insert into club (estado,codigo,tipo,nombre) values ('1','club-1','Baquetball','Club-1');
insert into club (estado,codigo,tipo,nombre) values ('2','club-2','Futbol','Club-2');

insert into usuarios (nombre,apellido,email,password,estado,enabled) values ('Admin','Global','estebanjpc@gmail.com','$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq',1,1);
insert into usuarios (nombre,apellido,email,password,estado,enabled,id_club) values ('Club-1',' AP','club1@gmail.com','$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq',1,1,1);
insert into usuarios (nombre,apellido,email,password,estado,enabled,id_club) values ('Club-1',' AP','club2@gmail.com','$2a$10$h5zWx.w.hq.a2ekp0S.BNe.cgoG4321.XcZYtJnPxANwJvw0xU2uq',1,1,2);

insert into usuarios_rol (id_usuario,authority) values (1,'ROLE_ADMIN');
insert into usuarios_rol (id_usuario,authority) values (2,'ROLE_CLUB');
insert into usuarios_rol (id_usuario,authority) values (3,'ROLE_CLUB');


insert into bancos (id,nombre) values (1,'Banco Estado');
insert into bancos (id,nombre) values (2,'BCI');
insert into bancos (id,nombre) values (3,'Banco Falabella');
insert into bancos (id,nombre) values (4,'Banco Chile');
insert into bancos (id,nombre) values (5,'Banco Itau');