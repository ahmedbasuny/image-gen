package com.ai.image.validation;

import com.ai.image.validation.validators.MaxFilesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxFilesValidator.class)
public @interface MaxFiles {
    String message() default "Maximum number of files exceeded";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int max() default 5;
}