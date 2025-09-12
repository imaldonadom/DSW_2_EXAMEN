-- LOOKUPS
INSERT INTO categorias (nombre) VALUES ('Fútbol'), ('Básquet');
INSERT INTO tipos_lamina (nombre) VALUES ('Normal'), ('Brillante');
INSERT INTO estados (nombre) VALUES ('incompleto'), ('completo');
INSERT INTO coleccionistas (nombre) VALUES ('Ignacio'), ('María');

-- (Opcional) si quieres un álbum de ejemplo sin láminas pre-creadas:
-- NOTA: Las láminas 1..N se crean cuando usas el ENDPOINT POST /api/v1/album/
-- porque esa lógica está en el service. Si insertas directo por SQL no se disparará.
-- Puedes dejar este insert o comentarlo y crear el álbum por API.
INSERT INTO albumes (nombre, fecha_lanzamiento, fecha_sorteo, categoria_id, cantidad_laminas, activo)
VALUES ('Álbum Copa 2025', CURRENT_DATE, CURRENT_DATE, 1, 10, TRUE);

-- Tags para ese álbum (id = 1 si es el primer insert)
INSERT INTO album_tags (album_id, tag) VALUES (1, 'deportes'), (1, 'copa');

-- Si quieres láminas vía SQL (en vez de crearlas por el endpoint),
-- descomenta esto y ajusta la cantidad:
-- INSERT INTO laminas (numero, tipo_lamina_id, album_id) VALUES
-- (1, 1, 1),(2, 1, 1),(3, 2, 1),(4, 1, 1),(5, 2, 1),(6, 1, 1),(7, 1, 1),(8, 2, 1),(9, 1, 1),(10, 1, 1);
