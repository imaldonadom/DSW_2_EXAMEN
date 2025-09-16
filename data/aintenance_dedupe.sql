-- Limpieza de álbumes duplicados por nombre (mantiene el de menor id)

-- 1) Mapa de keep/remove
WITH d AS (
  SELECT nombre, MIN(id) AS keep_id
  FROM albumes
  GROUP BY nombre
  HAVING COUNT(*) > 1
),
r AS (
  SELECT a.id AS remove_id, d.keep_id
  FROM albumes a
  JOIN d ON a.nombre = d.nombre
  WHERE a.id <> d.keep_id
)

-- 2) Mueve referencias en laminas
UPDATE laminas
SET album_id = (SELECT keep_id FROM r WHERE remove_id = laminas.album_id)
WHERE album_id IN (SELECT remove_id FROM r);

-- 3) Mueve referencias en tu tabla de colección (usa la que tengas)
-- Si tu tabla se llama coleccion_lamina:
UPDATE coleccion_lamina
SET album_id = (SELECT keep_id FROM r WHERE remove_id = coleccion_lamina.album_id)
WHERE album_id IN (SELECT remove_id FROM r);

-- Si en tu proyecto la colección está en coleccion_items, usa esta en su lugar:
-- UPDATE coleccion_items
-- SET album_id = (SELECT keep_id FROM r WHERE remove_id = coleccion_items.album_id)
-- WHERE album_id IN (SELECT remove_id FROM r);

-- 4) Borra los duplicados
DELETE FROM albumes
WHERE id IN (SELECT remove_id FROM (
  SELECT a.id AS remove_id
  FROM albumes a
  JOIN (
    SELECT nombre, MIN(id) AS keep_id
    FROM albumes
    GROUP BY nombre
    HAVING COUNT(*) > 1
  ) d ON a.nombre = d.nombre
  WHERE a.id <> d.keep_id
));
