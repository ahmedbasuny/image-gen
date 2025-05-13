package com.ai.image.validation.validators;

import com.ai.image.validation.MaxFiles;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MaxFilesValidator implements ConstraintValidator<MaxFiles, List<MultipartFile>> {
    private int max;

    @Override
    public void initialize(MaxFiles constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        return files == null || files.size() <= max;
    }
}