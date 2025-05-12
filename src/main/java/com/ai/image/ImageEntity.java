package com.ai.image;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "images")
public class ImageEntity {

    @Id
    private UUID id;
    private String imageName;
    private String mimeType;
    private String description;
    private String metaData;
    private String url;
}
