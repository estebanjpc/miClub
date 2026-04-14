-- Si la tabla `pago` ya existía con BLOB (~64 KB), ampliar el comprobante (ej. imágenes/PDF).
-- Ejecutar una vez en MySQL si no usas create-drop o si el esquema no se regeneró solo.
ALTER TABLE pago MODIFY COLUMN comprobante_transferencia MEDIUMBLOB NULL;
