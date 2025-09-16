PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS coleccion_lamina (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  coleccionista_id INTEGER NOT NULL,
  album_id         INTEGER NOT NULL,
  lamina_id        INTEGER NOT NULL,
  cantidad         INTEGER NOT NULL DEFAULT 0,
  created_at       TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE (coleccionista_id, album_id, lamina_id)
);

CREATE INDEX IF NOT EXISTS idx_coleccion_album
  ON coleccion_lamina (coleccionista_id, album_id);
