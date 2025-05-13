package com.ai.image.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageGenerationService {
    List<UUID> generateImages(String prompt, List<MultipartFile> images);
}
