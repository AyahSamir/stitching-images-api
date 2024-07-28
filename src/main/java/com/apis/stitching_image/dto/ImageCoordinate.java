package com.apis.stitching_image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ImageCoordinate {

    @NotNull
    float x;

    @NotNull
    float y;

}
