package com.ipss.et.controller;

import com.ipss.et.dto.ColeccionDtos.AltaUnitariaReq;
import com.ipss.et.dto.ColeccionDtos.TotalesResp;
import com.ipss.et.service.ColeccionLaminaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coleccionistas/{coleccionistaId}/albums/{albumId}/laminas")
@CrossOrigin(origins = "*")
public class ColeccionLaminaController {

    private final ColeccionLaminaService service;

    public ColeccionLaminaController(ColeccionLaminaService service) {
        this.service = service;
    }

    // ---------- ALTA UNITARIA ----------
    @PostMapping(value = "/unitario", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> altaUnitaria(@PathVariable Long coleccionistaId,
                                          @PathVariable Long albumId,
                                          @RequestBody AltaUnitariaReq req) {
        req.coleccionistaId = coleccionistaId;
        req.albumId = albumId;
        return ResponseEntity.ok(service.altaUnitaria(req));
    }

    // ---------- ALTA MASIVA CSV ----------
    // CSV esperado (con encabezado):
    // numero,cantidad
    // 7,1
    // 10,2
    @PostMapping(value = "/masivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> altaMasiva(@PathVariable Long coleccionistaId,
                                        @PathVariable Long albumId,
                                        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(service.altaMasiva(coleccionistaId, albumId, file));
    }

    // Descarga plantilla CSV
    @GetMapping("/plantilla")
    public ResponseEntity<byte[]> plantilla() {
        String csv = "numero,cantidad\r\n7,1\r\n10,2\r\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_coleccion.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- CONSULTAS ----------
    @GetMapping("/totales")
    public TotalesResp totales(@PathVariable Long coleccionistaId,
                               @PathVariable Long albumId) {
        return service.totales(coleccionistaId, albumId);
    }

    @GetMapping
    public List<Map<String, Object>> listar(@PathVariable Long coleccionistaId,
                                            @PathVariable Long albumId) {
        return service.listar(coleccionistaId, albumId);
    }

    // ---------- AJUSTES ----------
    @PatchMapping("/{id}/incrementar")
    public void inc(@PathVariable Long id) { service.ajustarCantidad(id, +1); }

    @PatchMapping("/{id}/decrementar")
    public void dec(@PathVariable Long id) { service.ajustarCantidad(id, -1); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
