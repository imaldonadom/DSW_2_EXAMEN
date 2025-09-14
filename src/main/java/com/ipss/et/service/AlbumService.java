package com.ipss.et.service;

import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.Album;
import com.ipss.et.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumes;

    @Transactional(readOnly = true)
    public List<AlbumDTO> list() {
        return albumes.findAll().stream().map(AlbumDTO::of).toList();
    }

    @Transactional(readOnly = true)
    public AlbumDTO get(Long id) {
        Album a = albumes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no existe"));
        return AlbumDTO.of(a);
    }
}
