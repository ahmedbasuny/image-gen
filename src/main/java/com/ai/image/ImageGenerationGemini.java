package com.ai.image;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationGemini implements ImageGenerationService {

    private final String imageGenerationModel = "gemini-2.0-flash-exp-image-generation";

    private final Client genaiClient;
    private final ImageStorageService imageStorageService;

    @Override
    public List<UUID> generateImages(String prompt, List<MultipartFile> images) {
        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt)); // Add prompt

        if (images != null) {
            List<Part> imageParts = images.stream()
                    .map(image -> {
                        try {
                            return Part.fromBytes(image.getBytes(), image.getContentType());
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            parts.addAll(imageParts); // Add images
        }

        Content content = Content.builder().parts(parts).build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(List.of("Text", "Image"))
                .build();

        try {
            GenerateContentResponse response =
                    this.genaiClient.models.generateContent(imageGenerationModel, content, config);
            List<Image> generatedImages = getImages(response);

            generatedImages.forEach(image ->
                    this.imageStorageService.store(image.imageId, image.imageName(), image.imageBytes(), image.mimeType));
            return generatedImages.stream().map(Image::imageId).toList();
        } catch (Exception e) {
            log.error("exception has thrown: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    private List<Image> getImages(GenerateContentResponse response) {
        ImmutableList<Part> responseParts = response.parts();
        if (responseParts == null || responseParts.isEmpty()) {
            return Collections.emptyList();
        }
        return responseParts
                .stream()
                .map(Part::inlineData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(inlineData -> inlineData.data().isPresent())
                .map(inlineData -> {
                    MimeType mimeType = MimeType.valueOf(inlineData.mimeType().get()); // imageMimeType
                    UUID imageId = UUID.randomUUID();
                    return new Image(
                            imageId,
                            "%s.%s".formatted(imageId.toString(), mimeType.getSubtype()),
                            inlineData.data().get(), // imageBytes
                            mimeType.toString());
                })
                .toList();
    }

    record Image(UUID imageId, String imageName, byte[] imageBytes, String mimeType) {}
}
