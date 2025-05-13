package com.ai.image.service;

import java.util.UUID;

public interface ImageStorageService {
    void store(UUID imageId, String imageName, byte[] bytes, String mimeType);

    byte[] getImage(String imageName);
}
