package com.ai.image.controller;

import com.ai.image.entites.ImageEntity;
import com.ai.image.service.ImageGenerationService;
import com.ai.image.repository.ImageRepository;
import com.ai.image.service.ImageStorageService;
import com.ai.image.exception.ImageNotFoundException;
import com.ai.image.exception.StorageException;
import com.ai.image.validation.MaxFiles;
import com.ai.image.validation.ValidFileTypes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageGenerationService imageGenerationService;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;

    @PostMapping
    public ResponseEntity<List<UUID>> generateImage(
            @RequestParam("prompt") @Valid @NotBlank @Size(max = 10000) String prompt,
            @RequestParam(value = "images", required = false) @Valid
            @MaxFiles(max = 5) @ValidFileTypes List<MultipartFile> images) {
        log.info("Received image generation request with prompt: {}", prompt);
        return new ResponseEntity<>(
                this.imageGenerationService.generateImages(prompt, images), HttpStatus.CREATED);
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable @Valid @Pattern(regexp = "^[a-fA-F0-9-]+\\.(png|jpg|jpeg|gif)$") String imageName) {
        Optional<ImageEntity> imageEntityOptional = imageRepository.findByImageName(imageName);
        if (imageEntityOptional.isEmpty()) {
            throw new ImageNotFoundException("Image not found with name: " + imageName);
        }
        byte[] bytes = this.imageStorageService.getImage(imageName);
        if (bytes == null || bytes.length == 0) {
            throw new StorageException("Failed to retrieve image: " + imageName);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .eTag(imageEntityOptional.get().getId().toString())
                .contentType(MediaType.valueOf(imageEntityOptional.get().getMimeType()))
                .body(bytes);
    }

    @GetMapping("/{imageName}/download")
    public ResponseEntity<byte[]> downloadImage(
            @PathVariable @Valid @Pattern(regexp = "^[a-fA-F0-9-]+\\.(png|jpg|jpeg|gif)$") String imageName) {
        Optional<ImageEntity> imageEntityOptional = imageRepository.findByImageName(imageName);
        if (imageEntityOptional.isEmpty()) {
            throw new ImageNotFoundException("Image not found with name: " + imageName);
        }
        byte[] bytes = this.imageStorageService.getImage(imageName);
        if (bytes == null || bytes.length == 0) {
            throw new StorageException("Failed to retrieve image: " + imageName);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
