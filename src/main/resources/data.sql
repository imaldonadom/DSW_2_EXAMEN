-- ===========================================
-- DATA SEED robusto para SQLite (singular/plural)
-- Guarda este archivo como UTF-8 (sin BOM)
-- ===========================================

-- ---------- Categorias (categoria/categorias) ----------
INSERT INTO categorias(id, nombre)
SELECT 1, 'Futbol'
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='categorias')
  AND NOT EXISTS (SELECT 1 FROM categorias WHERE id=1);

INSERT INTO categoria(id, nombre)
SELECT 1, 'Futbol'
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='categoria')
  AND NOT EXISTS (SELECT 1 FROM categoria WHERE id=1);

-- ---------- Album (albumes/albums/album) ----------
INSERT INTO albumes(id, nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo)
SELECT 1, 'copa libertadores 2026', 1, 1, 150, DATE('2025-09-12'), DATE('2025-10-01')
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='albumes')
  AND NOT EXISTS (SELECT 1 FROM albumes WHERE id=1);

INSERT INTO albums(id, nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo)
SELECT 1, 'copa libertadores 2026', 1, 1, 150, DATE('2025-09-12'), DATE('2025-10-01')
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='albums')
  AND NOT EXISTS (SELECT 1 FROM albums WHERE id=1);

INSERT INTO album(id, nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo)
SELECT 1, 'copa libertadores 2026', 1, 1, 150, DATE('2025-09-12'), DATE('2025-10-01')
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='album')
  AND NOT EXISTS (SELECT 1 FROM album WHERE id=1);

-- ---------- Coleccionista (solo plural normalmente) ----------
INSERT INTO coleccionistas(id, nombre)
SELECT 1, 'Default'
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='coleccionistas')
  AND NOT EXISTS (SELECT 1 FROM coleccionistas WHERE id=1);

-- ---------- Vínculo colecciones (colecciones) ----------
INSERT INTO colecciones(coleccionista_id, album_id)
SELECT 1, 1
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='colecciones')
  AND NOT EXISTS (SELECT 1 FROM colecciones WHERE coleccionista_id=1 AND album_id=1);

-- ---------- Catálogo de laminas (laminas/lamina) ----------
WITH RECURSIVE nums(n) AS (
  SELECT 1
  UNION ALL
  SELECT n+1 FROM nums WHERE n < 150
)
INSERT INTO laminas(album_id, numero)
SELECT 1, n
FROM nums
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='laminas')
  AND NOT EXISTS (SELECT 1 FROM laminas WHERE album_id=1 AND numero=n);

WITH RECURSIVE nums2(n) AS (
  SELECT 1
  UNION ALL
  SELECT n+1 FROM nums2 WHERE n < 150
)
INSERT INTO lamina(album_id, numero)
SELECT 1, n
FROM nums2
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE name='lamina')
  AND NOT EXISTS (SELECT 1 FROM lamina WHERE album_id=1 AND numero=n);
