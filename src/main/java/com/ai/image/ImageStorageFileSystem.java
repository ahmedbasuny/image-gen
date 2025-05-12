package com.ai.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageFileSystem implements ImageStorageService {

    private final String folder = "image-storage/";

    private final ImageRepository imageRepository;

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
