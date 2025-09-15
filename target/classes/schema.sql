-- Tabla de colección del coleccionista
CREATE TABLE IF NOT EXISTS coleccion_item (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  coleccionista_id INTEGER NOT NULL,
  album_id         INTEGER NOT NULL,
  lamina_id        INTEGER NOT NULL,
  cantidad         INTEGER NOT NULL DEFAULT 0,
  activo           INTEGER NOT NULL DEFAULT 1,
  FOREIGN KEY (album_id)  REFERENCES albumes(id),
  FOREIGN KEY (lamina_id) REFERENCES laminas(id)
);

-- Índice único: evita duplicados (por coleccionista, álbum y lámina)
CREATE UNIQUE INDEX IF NOT EXISTS ux_coleccion_unica
  ON coleccion_item (coleccionista_id, album_id, lamina_id);

-- Índice para listar rápido por coleccionista+álbum
CREATE INDEX IF NOT EXISTS idx_coleccion_album
  ON coleccion_item (coleccionista_id, album_id);
