package com.ai.image.service;

import com.ai.image.exception.StorageException;
import com.ai.image.repository.ImageRepository;
import com.ai.image.entites.ImageEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageFileSystem implements ImageStorageService {

    private final static String folder = "image-storage/";

    private final ImageRepository imageRepository;

    @PostConstruct
    public void init() {
        try {
            Path root = Paths.get(folder);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void store(UUID imageId, String imageName, byte[] bytes, String mimeType) {
        Path path = Paths.get(folder + imageName);
        try {
            Files.write(path, bytes);
            ImageEntity image = ImageEntity.builder()
                    .id(imageId)
                    .imageName(imageName)
                    .url(folder + imageName)
                    .mimeType(mimeType)
                    .build();
            imageRepository.save(image);
        } catch (IOException e) {
            log.error("I/O Error: {}", e.getMessage());
        }
    }

    @Override
    public byte[] getImage(String imageName) {
        Path path = Paths.get(folder + imageName);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("I/O Error: {}", e.getMessage());
            return null;
        }
    }
}
