package com.ai.image.repository;

import com.ai.image.entites.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    Optional<ImageEntity> findByImageName(String imageName);
}
