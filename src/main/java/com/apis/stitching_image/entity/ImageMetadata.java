package com.apis.stitching_image.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageMetadata {

    float x;

    float y;

    String imagePath;
}
