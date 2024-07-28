package com.apis.stitching_image.controller;

import com.apis.stitching_image.dto.Image;
import com.apis.stitching_image.dto.ImageCoordinate;
import com.apis.stitching_image.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/image")
@Slf4j
public class StitchingImageController {

    @Autowired
    ImageService imageService;

    @GetMapping(value="/stitched")
    public ResponseEntity<byte[]> getStitchedImage(ImageCoordinate imageCoordinate) throws IOException {
        try {
            byte[] stitchedImage = imageService.getStitchedImage(imageCoordinate);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(stitchedImage);
        } catch(Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addImage(@RequestPart("image") MultipartFile image, ImageCoordinate imageCoordinate) throws IOException {
        imageService.saveImage(new Image(image, imageCoordinate));
    }
}
