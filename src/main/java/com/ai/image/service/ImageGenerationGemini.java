package com.ai.image.service;

import com.ai.image.exception.GeminiServiceException;
import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationGemini implements ImageGenerationService {

    @Value("${gemini.model.image-generation:gemini-2.0-flash-exp-image-generation}")
    private String imageGenerationModel;

    @Value("${gemini.max.retries:3}")
    private int maxRetries;

    @Value("${gemini.retry.delay:1000}")
    private long retryDelay;

    private final Client genaiClient;
    private final ImageStorageService imageStorageService;

    @Override
    public List<UUID> generateImages(String prompt, List<MultipartFile> images) {
        try {
            List<Part> parts = buildRequestParts(prompt, images);
            GenerateContentResponse response = callGeminiApi(parts);
            return processResponse(response);
        } catch (Exception e) {
            log.error("Failed to generate images", e);
            throw new GeminiServiceException("Failed to generate images: " + e.getMessage());
        }
    }

    private List<Part> buildRequestParts(String prompt, List<MultipartFile> images) {
        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt));

        if (images != null) {
            parts.addAll(images.stream()
                    .map(this::convertToPart)
                    .filter(Objects::nonNull)
                    .toList());
        }
        return parts;
    }

    private Part convertToPart(MultipartFile image) {
        try {
            return Part.fromBytes(image.getBytes(), image.getContentType());
        } catch (IOException e) {
            log.warn("Failed to process image file", e);
            return null;
        }
    }

    private GenerateContentResponse callGeminiApi(List<Part> parts) {
        Content content = Content.builder().parts(parts).build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(List.of("Text", "Image"))
                .build();

        return this.genaiClient.models.generateContent(imageGenerationModel, content, config);
    }

    private List<UUID> processResponse(GenerateContentResponse response) {
        List<Image> generatedImages = getImages(response);

        if (generatedImages.isEmpty()) {
            throw new GeminiServiceException("No images were generated");
        }

        return generatedImages.stream()
                .map(this::storeImage)
                .toList();
    }

    private UUID storeImage(Image image) {
        this.imageStorageService.store(
                image.imageId(),
                image.imageName(),
                image.imageBytes(),
                image.mimeType()
        );
        return image.imageId();
    }

    private List<Image> getImages(GenerateContentResponse response) {
        if (response == null || response.parts() == null) {
            return Collections.emptyList();
        }

        return response.parts().stream()
                .map(Part::inlineData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(inlineData -> inlineData.data().isPresent())
                .map(this::createImageFromResponse)
                .toList();
    }

    private Image createImageFromResponse(Blob inlineData) {
        MimeType mimeType = MimeType.valueOf(inlineData.mimeType().get());
        UUID imageId = UUID.randomUUID();
        return new Image(
                imageId,
                "%s.%s".formatted(imageId.toString(), mimeType.getSubtype()),
                inlineData.data().get(),
                mimeType.toString()
        );
    }

    private static Function<Blob, Image> getImageFunction() {
        return inlineData -> {
            MimeType mimeType = MimeType.valueOf(inlineData.mimeType().get()); // imageMimeType
            UUID imageId = UUID.randomUUID();
            return new Image(
                    imageId,
                    "%s.%s".formatted(imageId.toString(), mimeType.getSubtype()),
                    inlineData.data().get(), // imageBytes
                    mimeType.toString());
        };
    }

    record Image(UUID imageId, String imageName, byte[] imageBytes, String mimeType) {
    }
}
