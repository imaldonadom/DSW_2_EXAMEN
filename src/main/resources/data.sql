-- src/main/resources/data.sql
PRAGMA foreign_keys = ON;

-- Categorías (no reinsertar si ya existen)
INSERT OR IGNORE INTO categorias (id, nombre) VALUES (1, 'Fútbol');
INSERT OR IGNORE INTO categorias (id, nombre) VALUES (2, 'Música');
INSERT OR IGNORE INTO categorias (id, nombre) VALUES (3, 'Cómics');

-- Semilla opcional de un álbum de ejemplo
INSERT OR IGNORE INTO albumes (
  id, nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo
) VALUES (
  1, 'Álbum Copa 2026', 1, 1, 10, DATE('2025-09-12'), DATE('2025-10-01')
);
