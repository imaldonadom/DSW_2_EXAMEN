package com.ipss.et.service;

import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.dto.AlbumCUDTO;

import java.util.List;

public interface AlbumService {
    List<AlbumDTO> listar();
    AlbumDTO crear(AlbumCUDTO in);
}
