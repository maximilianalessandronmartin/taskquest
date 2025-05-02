package org.novize.api.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;
import org.novize.api.dtos.UploadDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadService {
    private static final Logger LOGGER = LogManager.getLogger(UploadService.class);

    @Value("${backend-domain}")
    private String backenUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadDto uploadImage(MultipartFile file) {

        try {
            // Save the file to the directory
            String filename = saveImage(file);
            URI url = URI.create(backenUrl).resolve("/api/uploads/images/");

            String imageUrl = String.valueOf(url.resolve(filename));

            return UploadDto.builder()
                    .filename(filename)
                    .originalFilename(file.getOriginalFilename())
                    .fileUrl(imageUrl)
                    .success(true)
                    .build();
        } catch (IOException e) {
            return UploadDto.builder()
                    .success(false)
                    .build();
        }
    }

    public ResponseEntity<String> delete(String filename) {
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(filename);
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                return ResponseEntity.ok("Success");
            } catch (IOException e) {
                LOGGER.warn("File not deleted: {}", filename);
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed");
    }


    BufferedImage simpleResizeImage(BufferedImage originalImage, int targetWidth) {
        return Scalr.resize(originalImage, targetWidth);
    }

    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        final String uuid = String.valueOf(UUID.randomUUID());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = simpleResizeImage(bufferedImage, 400);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "JPG", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        Path filePath = uploadPath.resolve(String.valueOf(uuid));
        Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);

        return uuid;
    }
}
