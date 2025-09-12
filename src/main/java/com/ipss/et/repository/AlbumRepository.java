package com.ipss.et.repository;

import com.ipss.et.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> { }
