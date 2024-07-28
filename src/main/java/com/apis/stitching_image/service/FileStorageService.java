package com.apis.stitching_image.service;

import com.apis.stitching_image.dto.Image;
import com.apis.stitching_image.dto.ImageCoordinate;
import com.apis.stitching_image.entity.ImageMetadata;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class FileStorageService {

    @Value("${paths.images.file.metadata}")
    private String imagesMetadataFilePath;

    public void saveImageMetadata(Image image, String localImagePath) throws IOException {
        try (FileWriter f = new FileWriter(imagesMetadataFilePath, true)) {
            String imageMetadataRecord = String.join(",",
                    String.valueOf(image.getCoordinates().getX()),
                    String.valueOf(image.getCoordinates().getY()),
                    localImagePath);

            f.write(imageMetadataRecord + "\n");
        }
    }

    public Optional<ImageMetadata> getImageMetadata(float x, float y) throws IOException {
        List<String> lines = FileUtils.readLines(new File(imagesMetadataFilePath), StandardCharsets.UTF_8);

        String searchRecord = String.join(",",
                String.valueOf(x),
                String.valueOf(y));

        for (String line : lines) {
            if (line.startsWith(searchRecord)){
                return Optional.of(parseRecord(line));
            }
        }
        return Optional.empty();
    }


    private ImageMetadata parseRecord(String record){
        List<String> imageMetadata = List.of(record.split(","));
        return new ImageMetadata(Float.parseFloat(imageMetadata.get(0))
                                , Float.parseFloat(imageMetadata.get(1))
                                , imageMetadata.get(2));
    }

}
