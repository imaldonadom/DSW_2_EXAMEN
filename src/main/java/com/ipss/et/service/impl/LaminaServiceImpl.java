package com.ipss.et.service.impl;

import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import com.ipss.et.model.TipoLamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.LaminaRepository;
import com.ipss.et.repository.TipoLaminaRepository;
import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class LaminaServiceImpl implements LaminaService {

  private final LaminaRepository laminas;
  private final AlbumRepository albumes;
  private final TipoLaminaRepository tipos;

  @Override
  @Transactional
  public Map<String, Integer> generar(Long albumId, Long tipoId, Integer cantidad) {
    Album a = albumes.findById(albumId)
        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Álbum inválido"));
    TipoLamina t = tipos.findById(tipoId)
        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Tipo inválido"));

    int inserted = 0, skipped = 0;

    for (int i = 1; i <= cantidad; i++) {
      // IMPORTANTE: variable efectivamente final para usar dentro de la lambda
      final int numero = i;

      Lamina existente = laminas.findAll().stream()
          .filter(x -> x.getAlbum() != null
              && albumId.equals(x.getAlbum().getId())
              && x.getNumero() != null
              && x.getNumero() == numero)
          .findFirst()
          .orElse(null);

      if (existente != null) {
        skipped++;
        continue;
      }

      Lamina l = new Lamina();
      l.setAlbum(a);
      l.setTipo(t);
      l.setNumero(numero);
      laminas.save(l);
      inserted++;
    }

    return Map.of("inserted", inserted, "skipped", skipped);
  }

  @Override
  @Transactional
  public Map<String, Integer> bulkUpsert(Long albumId, List<Item> items) {
    Album a = albumes.findById(albumId)
        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Álbum inválido"));

    int inserted = 0, updated = 0, skipped = 0;

    for (Item it : items) {
      if (it == null || it.numero() == null) {
        skipped++;
        continue;
      }

      Lamina l = laminas.findAll().stream()
          .filter(x -> x.getAlbum() != null
              && albumId.equals(x.getAlbum().getId())
              && it.numero().equals(x.getNumero()))
          .findFirst()
          .orElse(null);

      TipoLamina t = null;
      if (it.tipoId() != null) {
        t = tipos.findById(it.tipoId())
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Tipo inválido: " + it.tipoId()));
      }

      if (l == null) {
        l = new Lamina();
        l.setAlbum(a);
        l.setNumero(it.numero());
        l.setTipo(t);
        laminas.save(l);
        inserted++;
      } else {
        l.setTipo(t); // dirty checking
        updated++;
      }
    }

    return Map.of("inserted", inserted, "updated", updated, "skipped", skipped);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Lamina> list(Long albumId) {
    return laminas.findAll().stream()
        .filter(x -> x.getAlbum() != null && albumId.equals(x.getAlbum().getId()))
        .sorted(Comparator.comparing(Lamina::getNumero))
        .toList();
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!laminas.existsById(id)) {
      throw new ResponseStatusException(BAD_REQUEST, "Lámina no existe: " + id);
    }
    laminas.deleteById(id);
  }
}
