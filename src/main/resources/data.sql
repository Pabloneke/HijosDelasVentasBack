-- 1. 4 Categorías
INSERT IGNORE INTO categoria (id, nombre) VALUES (1, 'Computación');
INSERT IGNORE INTO categoria (id, nombre) VALUES (2, 'Gaming');
INSERT IGNORE INTO categoria (id, nombre) VALUES (3, 'Librería');
INSERT IGNORE INTO categoria (id, nombre) VALUES (4, 'Perfumería');

-- 2. 6 Productos (idénticos a db.js)
INSERT IGNORE INTO producto (id, name, price, image, stock, stock_critico, categoria_id) VALUES
(1, 'Perfume Rasasi Hawas', 65000, '/img/RasasiHawas.webp', 12, 3, 4),
(2, 'MousePad XXL Gamer', 15000, '/img/MousePadXXL.webp', 25, 5, 1),
(3, 'Teclado Mecánico RGB', 45000, '/img/Teclado.webp', 4, 5, 1),
(4, 'Juego PS4', 20000, '/img/PS14.webp', 8, 3, 2),
(5, 'GTA VI (Pre-venta)', 70000, '/img/GTA-VI.webp', 50, 10, 2),
(6, 'Manga Dorohedoro Vol. 1', 12000, '/img/Dorohedoro1.webp', 2, 2, 3);

-- 3. 3 Usuarios con contraseñas BCrypt:
-- admin@duoc.cl -> admin123
-- vendedor@duoc.cl -> vende123
-- carla@gmail.com -> cliente123
INSERT IGNORE INTO usuario (run, nombre, apellidos, correo, password, tipo, direccion, region, comuna) VALUES
('190110222', 'Paulo', 'Catalán', 'admin@duoc.cl', '$2a$10$kJ49AmwaKzySnsqdXvHhr.5qjmAa0stOmYLBxgPb/Gya31dcpEkdu', 'administrador', 'Av. Siempre Viva 123', 'Región Metropolitana de Santiago', 'Santiago'),
('182345679', 'Mauricio', 'Ahumada', 'vendedor@duoc.cl', '$2a$10$wh4DHUMSpSPfdhQ7U16EmOXeYOy7ulB956n.nOzT.04ZzHECe.G72', 'vendedor', 'Calle Los Alerces 456', 'Región del Biobío', 'Concepción'),
('20987654K', 'Carla', 'Gómez', 'carla@gmail.com', '$2a$10$gtBdK8nQLTtdhkrqCt3czeEGNLzsvYo7ljyY/XaSRIf9hKJfAUJky', 'cliente', 'Pasaje Las Rosas 789', 'Región de Valparaíso', 'Viña del Mar');

-- Corrige los hashes originales en bases ya inicializadas.
UPDATE usuario SET password = '$2a$10$kJ49AmwaKzySnsqdXvHhr.5qjmAa0stOmYLBxgPb/Gya31dcpEkdu'
WHERE correo = 'admin@duoc.cl' AND run = '190110222'
  AND CAST(password AS BINARY(255)) = CAST('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS BINARY(255));

UPDATE usuario SET password = '$2a$10$wh4DHUMSpSPfdhQ7U16EmOXeYOy7ulB956n.nOzT.04ZzHECe.G72'
WHERE correo = 'vendedor@duoc.cl' AND run = '182345679'
  AND CAST(password AS BINARY(255)) = CAST('$2a$10$8K1p/a0dL3.0R/2k4M8L0eR5h8O2k4M8L0eR5h8O2k4M8L0eR5h8O' AS BINARY(255));

UPDATE usuario SET password = '$2a$10$gtBdK8nQLTtdhkrqCt3czeEGNLzsvYo7ljyY/XaSRIf9hKJfAUJky'
WHERE correo = 'carla@gmail.com' AND run = '20987654K'
  AND CAST(password AS BINARY(255)) = CAST('$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5G1E5O2k4M8L0eR5h8O2k4M8L0eR5' AS BINARY(255));
