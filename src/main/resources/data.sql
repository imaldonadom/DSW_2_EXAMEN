-- =========================================================
-- data.sql  (UTF-8 sin BOM)  |  SQLite
-- Seed idempotente para pruebas locales
-- Tablas usadas (en plural): categorias, albumes, laminas,
-- coleccionistas, colecciones, tipos_lamina
-- =========================================================

-- ---------- CATEGORÍAS ----------
INSERT OR IGNORE INTO categorias(id, nombre) VALUES
  (1, 'Fútbol'),
  (2, 'Música'),
  (3, 'Manga'),
  (4, 'Cómics');

-- ---------- TIPOS DE LÁMINA ----------
INSERT OR IGNORE INTO tipos_lamina(id, nombre) VALUES
  (1, 'Normal'),
  (2, 'Brillante'),
  (3, 'Dorada');

-- ---------- ÁLBUMES ----------
-- Nota: ajusta fechas si quieres. "activo" = 1 (sí).
INSERT OR IGNORE INTO albumes
  (id, nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo)
VALUES
  (1, 'copa libertadores 2026', 1, 1, 150, DATE('2025-09-12'), DATE('2025-10-01')),
  (5, 'Albun Copa Gato',       1, 1, 100, DATE('2025-10-01'), DATE('2025-11-01')),
  (6, 'ACDC',                  2, 1, 100, DATE('2025-09-01'), DATE('2026-03-01')),
  (7, 'Naruto',                3, 1, 100, DATE('2026-01-01'), DATE('2026-09-01')),
  (8, 'One Piece',             3, 1, 100, DATE('2025-10-01'), DATE('2026-03-01')),
  (9, 'Super Man',             4, 1,  70, DATE('2025-10-01'), DATE('2026-03-01'));

-- ---------- COLECCIONISTA DEFECTO ----------
INSERT OR IGNORE INTO coleccionistas(id, nombre)
VALUES (1, 'Default');

-- ---------- ENLACES COLECCIÓN (coleccionista 1 con todos los álbumes) ----------
-- (idempotente)
INSERT OR IGNORE INTO colecciones(coleccionista_id, album_id) VALUES
  (1, 1),
  (1, 5),
  (1, 6),
  (1, 7),
  (1, 8),
  (1, 9);

-- ---------- CATÁLOGO DE LÁMINAS 1..N POR ÁLBUM ----------
-- Genera para todos los álbumes según "cantidad_laminas".
-- Si ya existen, no duplica.
WITH RECURSIVE seq(n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1
  FROM seq
  WHERE n < 300        -- límite superior (>= al mayor cantidad_laminas)
)
INSERT INTO laminas(album_id, numero)
SELECT a.id, s.n
FROM albumes a
JOIN seq s            ON s.n <= a.cantidad_laminas
WHERE NOT EXISTS (
  SELECT 1
  FROM laminas l
  WHERE l.album_id = a.id
    AND l.numero   = s.n
);

WITH RECURSIVE seq(n) AS (
  SELECT 1
  UNION ALL SELECT n+1 FROM seq WHERE n < 2000   -- sube el tope si necesitas
)
INSERT INTO laminas (album_id, numero)
SELECT a.id, s.n
FROM albumes a
JOIN seq s               ON s.n <= a.cantidad_laminas
LEFT JOIN laminas l      ON l.album_id = a.id AND l.numero = s.n
WHERE l.id IS NULL;