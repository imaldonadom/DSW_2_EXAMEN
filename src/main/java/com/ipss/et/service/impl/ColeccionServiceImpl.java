package com.ipss.et.service.impl;

import com.ipss.et.model.Album;
import com.ipss.et.model.ColeccionItem;
import com.ipss.et.model.Lamina;
import com.ipss.et.repository.ColeccionItemRepository;
import com.ipss.et.repository.LaminaRepository;
import com.ipss.et.service.ColeccionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ColeccionServiceImpl implements ColeccionService {

    private final ColeccionItemRepository repo;
    private final LaminaRepository laminaRepo;

    public ColeccionServiceImpl(ColeccionItemRepository repo, LaminaRepository laminaRepo) {
        this.repo = repo;
        this.laminaRepo = laminaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Row> view(Long coleccionistaId, Long albumId) {
        // Todas las láminas del álbum (para saber qué falta)
        List<Lamina> laminas = laminaRepo.findByAlbumIdOrderByNumeroAsc(albumId);

        // Las cantidades ya guardadas por el coleccionista
        Map<Long, ColeccionItem> byLamina = repo
                .findByColeccionistaIdAndAlbumId(coleccionistaId, albumId)
                .stream()
                .collect(Collectors.toMap(ci -> ci.getLamina().getId(), ci -> ci));

        List<Row> out = new ArrayList<>();
        for (Lamina l : laminas) {
            Row r = new Row();
            r.laminaId = l.getId();
            r.numero = l.getNumero();
            r.tipo = (l.getTipo() != null) ? l.getTipo().getNombre() : "";
            ColeccionItem ci = byLamina.get(l.getId());
            int cant = (ci != null && ci.getCantidad() != null) ? ci.getCantidad() : 0;
            r.cantidad = cant;
            r.duplicadas = Math.max(0, cant - 1);
            r.falta = cant <= 0;
            out.add(r);
        }
        return out;
    }

    @Override
    @Transactional
    public int bulkSet(Long coleccionistaId, Long albumId, List<ItemQ> items) {
        if (items == null || items.isEmpty()) return 0;

        // Índice actual para reusar o borrar
        Map<Long, ColeccionItem> actuales = repo
                .findByColeccionistaIdAndAlbumId(coleccionistaId, albumId)
                .stream()
                .collect(Collectors.toMap(ci -> ci.getLamina().getId(), ci -> ci));

        int updated = 0;
        for (ItemQ it : items) {
            if (it == null || it.laminaId == null) continue;
            int cant = (it.cantidad == null || it.cantidad < 0) ? 0 : it.cantidad;

            ColeccionItem ci = actuales.get(it.laminaId);
            if (cant == 0) {
                if (ci != null) {
                    repo.delete(ci);
                    updated++;
                }
                continue;
            }

            if (ci == null) {
                ci = new ColeccionItem();
                ci.setColeccionistaId(coleccionistaId);
                Lamina lam = new Lamina();
                lam.setId(it.laminaId);
                ci.setLamina(lam);
                Album alb = new Album();
                alb.setId(albumId);
                ci.setAlbum(alb);
            }
            ci.setCantidad(cant);
            ci.setActivo(true);
            repo.save(ci);
            updated++;
        }
        return updated;
    }

    @Override
    @Transactional
    public int inc(Long coleccionistaId, Long albumId, Long laminaId, int delta) {
        int d = (delta == 0) ? 0 : (delta > 0 ? 1 : -1);

        Optional<ColeccionItem> opt = repo
                .findByColeccionistaIdAndAlbumIdAndLaminaId(coleccionistaId, albumId, laminaId);

        ColeccionItem ci = opt.orElseGet(() -> {
            ColeccionItem n = new ColeccionItem();
            n.setColeccionistaId(coleccionistaId);
            Album a = new Album(); a.setId(albumId); n.setAlbum(a);
            Lamina l = new Lamina(); l.setId(laminaId); n.setLamina(l);
            n.setCantidad(0);
            return n;
        });

        int nueva = Math.max(0, (ci.getCantidad() == null ? 0 : ci.getCantidad()) + d);
        if (nueva == 0 && ci.getId() != null) {
            repo.delete(ci);
            return 1;
        }
        ci.setCantidad(nueva);
        ci.setActivo(true);
        repo.save(ci);
        return 1;
    }

    @Override
    @Transactional
    public int clearAlbum(Long coleccionistaId, Long albumId) {
        return repo.deleteByColeccionistaIdAndAlbumId(coleccionistaId, albumId);
    }
}
