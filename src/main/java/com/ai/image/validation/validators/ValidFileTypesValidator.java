package com.ai.image.validation.validators;

import com.ai.image.validation.ValidFileTypes;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidFileTypesValidator implements ConstraintValidator<ValidFileTypes, List<MultipartFile>> {
    private Set<String> allowedTypes;

    @Override
    public void initialize(ValidFileTypes constraintAnnotation) {
        this.allowedTypes = new HashSet<>(Arrays.asList(constraintAnnotation.types()));
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null) return true;

        return files.stream().allMatch(file -> {
            String contentType = file.getContentType();
            return contentType != null && allowedTypes.contains(contentType);
        });
    }
}