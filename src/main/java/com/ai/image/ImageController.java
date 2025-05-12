package com.ai.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageGenerationService imageGenerationService;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;

    @PostMapping
    public List<UUID> generateImage(@RequestParam("prompt") String prompt,
                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return this.imageGenerationService.generateImages(prompt, images);
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        Optional<ImageEntity> imageEntityOptional= imageRepository.findByImageName(imageName);
        byte[] bytes = this.imageStorageService.getImage(imageName);
        return ResponseEntity.ok()
                .contentType(
                        imageEntityOptional
                                .map(imageEntity -> MediaType.valueOf(imageEntity.getMimeType()))
                                .orElse(MediaType.IMAGE_PNG))
                .body(bytes);
    }

    @GetMapping("/{imageName}/download")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String imageName) {
        byte[] bytes = this.imageStorageService.getImage(imageName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
