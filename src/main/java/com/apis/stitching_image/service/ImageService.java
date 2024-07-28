package com.apis.stitching_image.service;

import com.apis.stitching_image.dto.Image;
import com.apis.stitching_image.dto.ImageCoordinate;
import com.apis.stitching_image.entity.ImageMetadata;
import com.apis.stitching_image.exception.ImageStitchingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.opencv.opencv_stitching.Stitcher;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_stitching.createStitcher;

@Service
@Slf4j
public class ImageService {

    final static String RESULT_FILE_EXTENSION = ".jpg";

    @Value("${paths.images.upload}")
    private String uploadDirectory;

    @Value("${paths.images.results}")
    private String resultsDirectory;

    @Autowired
    FileStorageService fileStorageService;

    public void saveImage(Image image) throws IOException {
        Path uploadDirectoryPath = Paths.get(uploadDirectory);
        Path uploadFilePath = uploadDirectoryPath.resolve(getRandomName());

        if(!Files.exists(uploadDirectoryPath)){
            Files.createDirectories(uploadDirectoryPath);
        }

        Files.copy(image.getContent().getInputStream(), uploadFilePath);

        fileStorageService.saveImageMetadata(image, uploadFilePath.toString());
    }


    public byte[] getStitchedImage(ImageCoordinate imageCoordinate) throws IOException {
        List<String> imagesPath = getStitchedImagesPaths(imageCoordinate);
        Path getResultFilePath = getResultFilePath();
        try (MatVector images = new MatVector()) {
            for (String path : imagesPath) {
                Mat mat = imread(path);
                images.resize(images.size() + 1);
                images.put(images.size() - 1, mat);
            }

            Mat stitched = new Mat();
            Stitcher stitcher = createStitcher();

            int status = stitcher.stitch(images, stitched);
            if (status != Stitcher.OK) {
                log.error("Can't stitch images, error code = {}", status);
                throw new ImageStitchingException("Can't stitch images");
            }

            imwrite(getResultFilePath.toString(), stitched);
            return FileUtils.readFileToByteArray(new File(getResultFilePath.toString()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImageStitchingException("Can't stitch images, please make sure images exist and overlapped enough to be stitched.");
        }finally {
            Files.deleteIfExists(getResultFilePath);
        }
    }

    private List<String> getStitchedImagesPaths(ImageCoordinate imageCoordinate) throws IOException {
        Optional<ImageMetadata> firstImage = fileStorageService.getImageMetadata(imageCoordinate.getX(), imageCoordinate.getY());
        Optional<ImageMetadata> secImage = fileStorageService.getImageMetadata(imageCoordinate.getX()+1 , imageCoordinate.getY());
        Optional<ImageMetadata> thirdImage = fileStorageService.getImageMetadata(imageCoordinate.getX(), imageCoordinate.getY() +1);
        Optional<ImageMetadata> fourthImage = fileStorageService.getImageMetadata(imageCoordinate.getX() + 1, imageCoordinate.getY() + 1);

        List<String> imagesPaths = new ArrayList<>();
        firstImage.ifPresent(imageMetadata -> imagesPaths.add(imageMetadata.getImagePath()));
        secImage.ifPresent(imageMetadata -> imagesPaths.add(imageMetadata.getImagePath()));
        thirdImage.ifPresent(imageMetadata -> imagesPaths.add(imageMetadata.getImagePath()));
        fourthImage.ifPresent(imageMetadata -> imagesPaths.add(imageMetadata.getImagePath()));

        if(imagesPaths.isEmpty()){
            throw new ImageStitchingException("Not enough images");
        }

        return imagesPaths;
    }

    private Path getResultFilePath() throws IOException {
        Path resultsDirectoryPath = Paths.get(resultsDirectory);
        Path resultFilePath = resultsDirectoryPath.resolve(getRandomName() + RESULT_FILE_EXTENSION);

        if(!Files.exists(resultsDirectoryPath)){
            Files.createDirectories(resultsDirectoryPath);
        }

        return resultFilePath;
    }

    private static String getRandomName(){
        return UUID.randomUUID().toString();
    }
}
