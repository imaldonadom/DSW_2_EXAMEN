-- PRAGMA y FK
PRAGMA foreign_keys = ON;

-- =========================
-- CATEGORÍAS
-- =========================
INSERT OR IGNORE INTO categorias (id, nombre) VALUES
 (1, 'Fútbol'),
 (2, 'Música'),
 (3, 'Cómics'),
 (4, 'Manga'),
 (5, 'Película');

-- =========================
-- TIPOS DE LÁMINA
-- =========================
DELETE FROM tipos_lamina WHERE nombre NOT IN ('Normal','Brillante','Dorada');

INSERT OR IGNORE INTO tipos_lamina (id, nombre) VALUES (1, 'Normal');
INSERT OR IGNORE INTO tipos_lamina (id, nombre) VALUES (2, 'Brillante');
INSERT OR IGNORE INTO tipos_lamina (id, nombre) VALUES (3, 'Dorada');

-- =========================
-- ÁLBUM SEMILLA (solo si la tabla está vacía)
-- =========================
INSERT INTO albumes (nombre, categoria_id, activo, cantidad_laminas, fecha_lanzamiento, fecha_sorteo)
SELECT 'Álbum Copa 2026', 1, 1, 10, DATE('2025-09-12'), DATE('2025-10-01')
WHERE NOT EXISTS (SELECT 1 FROM albumes);
