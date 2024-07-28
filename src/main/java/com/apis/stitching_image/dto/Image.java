package com.apis.stitching_image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class Image {

    @NotNull
    MultipartFile content;

    @NotNull
    ImageCoordinate coordinates;

}
