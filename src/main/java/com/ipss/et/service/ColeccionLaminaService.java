package com.ipss.et.service;

import com.ipss.et.dto.ColeccionDtos.AltaUnitariaReq;
import com.ipss.et.dto.ColeccionDtos.AltaUnitariaResp;
import com.ipss.et.dto.ColeccionDtos.TotalesResp;
import com.ipss.et.model.Album;
import com.ipss.et.model.ColeccionLamina;
import com.ipss.et.model.Lamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.ColeccionLaminaRepository;
import com.ipss.et.repository.LaminaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ColeccionLaminaService {

    private final ColeccionLaminaRepository repo;
    private final AlbumRepository albumRepo;
    private final LaminaRepository laminaRepo;

    public ColeccionLaminaService(ColeccionLaminaRepository repo,
                                  AlbumRepository albumRepo,
                                  LaminaRepository laminaRepo) {
        this.repo = repo;
        this.albumRepo = albumRepo;
        this.laminaRepo = laminaRepo;
    }

    /** <<< CAMBIADO: ahora devuelve DTO liviano >>> */
    @Transactional
    public AltaUnitariaResp altaUnitaria(AltaUnitariaReq req) {
        if (req.cantidad == null || req.cantidad <= 0) req.cantidad = 1;

        Album album = albumRepo.findById(req.albumId)
                .orElseThrow(() -> new IllegalArgumentException("Álbum no encontrado: " + req.albumId));

        Lamina lamina = laminaRepo.findByAlbumIdAndNumero(req.albumId, req.laminaNumero)
                .orElseThrow(() -> new IllegalArgumentException("Lámina N° " + req.laminaNumero + " no existe en el álbum"));

        Optional<ColeccionLamina> opt = repo.findByColeccionistaIdAndAlbumAndLamina(req.coleccionistaId, album, lamina);
        ColeccionLamina cl = opt.orElseGet(() -> new ColeccionLamina(req.coleccionistaId, album, lamina, 0));
        cl.setCantidad(cl.getCantidad() + req.cantidad);
        cl = repo.save(cl);

        // devolvemos SOLO lo necesario (ya dentro de la misma transacción)
        return new AltaUnitariaResp(cl.getId(), lamina.getNumero(), cl.getCantidad());
    }

    @Transactional
    public Map<String, Object> altaMasiva(Long coleccionistaId, Long albumId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Archivo CSV vacío");
        Album album = albumRepo.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Álbum no encontrado: " + albumId));

        int ok = 0, err = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) throw new IllegalArgumentException("CSV sin contenido");
            int linea = 1;

            String row;
            while ((row = br.readLine()) != null) {
                linea++;
                String[] parts = row.split(",", -1);
                if (parts.length < 1) { err++; errores.add("Línea " + linea + ": vacía"); continue; }

                Integer numero;
                Integer cantidad = 1;

                try {
                    numero = Integer.parseInt(parts[0].trim());
                    if (parts.length > 1 && !parts[1].trim().isEmpty())
                        cantidad = Integer.parseInt(parts[1].trim());
                    if (cantidad <= 0) cantidad = 1;
                } catch (Exception ex) {
                    err++; errores.add("Línea " + linea + ": número/cantidad inválidos");
                    continue;
                }

                Optional<Lamina> opLam = laminaRepo.findByAlbumIdAndNumero(albumId, numero);
                if (opLam.isEmpty()) { err++; errores.add("Línea " + linea + ": lámina " + numero + " no existe"); continue; }

                Lamina lamina = opLam.get();
                Optional<ColeccionLamina> opt = repo.findByColeccionistaIdAndAlbumAndLamina(coleccionistaId, album, lamina);
                ColeccionLamina cl = opt.orElseGet(() -> new ColeccionLamina(coleccionistaId, album, lamina, 0));
                cl.setCantidad(cl.getCantidad() + cantidad);
                repo.save(cl);
                ok++;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error leyendo CSV: " + ex.getMessage(), ex);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("procesadas", ok + err);
        r.put("ok", ok);
        r.put("error", err);
        r.put("errores", errores);
        return r;
    }

    @Transactional(readOnly = true)
    public TotalesResp totales(Long coleccionistaId, Long albumId) {
        List<ColeccionLamina> lista = repo.findByColeccionistaIdAndAlbum_Id(coleccionistaId, albumId);

        int totalAlbum = (int) laminaRepo.countByAlbumId(albumId);
        int coleccionadas = (int) lista.stream().filter(cl -> cl.getCantidad() != null && cl.getCantidad() > 0).count();
        int duplicadas = lista.stream()
                .mapToInt(cl -> Math.max((cl.getCantidad() == null ? 0 : cl.getCantidad()) - 1, 0))
                .sum();

        int faltan = Math.max(totalAlbum - coleccionadas, 0);
        return new TotalesResp(totalAlbum, coleccionadas, faltan, duplicadas);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(Long coleccionistaId, Long albumId) {
        return repo.findByColeccionistaIdAndAlbum_Id(coleccionistaId, albumId)
                .stream()
                .map(cl -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", cl.getId());
                    m.put("numero", cl.getLamina().getNumero());
                    m.put("album", cl.getAlbum().getNombre());
                    m.put("tipo", cl.getLamina().getTipo());
                    m.put("cantidad", cl.getCantidad());
                    return m;
                }).sorted(Comparator.comparingInt(m -> (Integer) m.get("numero")))
                .collect(Collectors.toList());
    }

    @Transactional
    public void ajustarCantidad(Long id, int delta) {
        ColeccionLamina cl = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));
        int nueva = Math.max(0, cl.getCantidad() + delta);
        cl.setCantidad(nueva);
        repo.save(cl);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
