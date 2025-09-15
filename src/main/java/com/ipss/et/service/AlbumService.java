package com.ipss.et.service;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;

import java.util.List;

public interface AlbumService {
    List<AlbumDTO> list();
    AlbumDTO create(AlbumCUDTO in);
    AlbumDTO update(Long id, AlbumCUDTO in);
    void delete(Long id);
}
