package com.ipss.et.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminMaintenanceController {

    private final JdbcTemplate jdbc;

    public AdminMaintenanceController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 1) Ver duplicados por nombre (te devuelve nombre + ids + cantidad) */
    @GetMapping("/album-duplicates")
    public List<Map<String, Object>> albumDuplicates() {
        String sql = """
            SELECT nombre,
                   GROUP_CONCAT(id) AS ids,
                   COUNT(*)          AS c
            FROM albumes
            GROUP BY nombre
            HAVING COUNT(*) > 1
            ORDER BY nombre
            """;
        return jdbc.queryForList(sql);
    }


    @PostMapping("/album-merge")
    @Transactional
    public Map<String, Object> albumMerge(@RequestParam Long keepId,
                                          @RequestParam Long removeId) {
        int movedLaminas    = jdbc.update("UPDATE laminas          SET album_id=? WHERE album_id=?", keepId, removeId);
        int movedColeccion  = jdbc.update("UPDATE coleccion_lamina SET album_id=? WHERE album_id=?", keepId, removeId);
        int deletedAlbums   = jdbc.update("DELETE FROM albumes WHERE id=?", removeId);

        jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_album_nombre               ON albumes(nombre)");
        jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_album_nombre_categoria     ON albumes(nombre, categoria_id)");

        return Map.of(
                "moved_laminas", movedLaminas,
                "moved_coleccion_items", movedColeccion,
                "deleted_albums", deletedAlbums
        );
    }
}
