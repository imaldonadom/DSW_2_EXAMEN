package com.ipss.et.controller;

import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lamina")
@RequiredArgsConstructor
public class LaminaController {

  private final LaminaService laminas;

  // LISTADO por álbum (lo que consume app-laminas.js)
  @GetMapping
  public List<Map<String, Object>> list(@RequestParam Long albumId) {
    return laminas.list(albumId).stream().map(l -> Map.of(
        "id", l.getId(),
        "numero", l.getNumero(),
        "album", Map.of(
            "id", l.getAlbum().getId(),
            "nombre", l.getAlbum().getNombre()
        ),
        "tipo", (l.getTipo() == null ? null : Map.of(
            "id", l.getTipo().getId(),
            "nombre", l.getTipo().getNombre()
        ))
    )).toList();
  }

  // GENERAR por rango 1..cantidad con tipo seleccionado
  @PostMapping("/generar")
  public Map<String, Integer> generar(@RequestParam Long albumId,
                                      @RequestParam Long tipoId,
                                      @RequestParam Integer cantidad) {
    return laminas.generar(albumId, tipoId, cantidad);
  }

  // BULK desde CSV/JSON [{numero,tipoId},...]
  @PostMapping("/bulk")
  public Map<String, Integer> bulk(@RequestParam Long albumId,
                                   @RequestBody List<LaminaService.Item> items) {
    return laminas.bulkUpsert(albumId, items);
  }

  // BORRAR por id
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    laminas.delete(id);
  }

  // DESCARGA PLANTILLA CSV
  @GetMapping(value = "/plantilla", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<byte[]> plantilla(@RequestParam(defaultValue = "60") Integer cantidad) {
    int n = (cantidad == null || cantidad <= 0) ? 60 : cantidad;
    StringBuilder sb = new StringBuilder("numero,tipoId\n");
    for (int i = 1; i <= n; i++) {
      sb.append(i).append(",").append("").append("\n");
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"laminas_plantilla.csv\"")
        .contentType(MediaType.TEXT_PLAIN)
        .body(bytes);
  }
}
