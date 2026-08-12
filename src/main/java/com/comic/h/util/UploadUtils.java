package com.comic.h.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

public class UploadUtils {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    private UploadUtils() {
        // Private constructor for utility class
    }

    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name format");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Invalid image file format. Allowed formats: " + ALLOWED_EXTENSIONS);
        }

        BufferedImage inputImage;
        try (InputStream inputStream = file.getInputStream()) {
            inputImage = ImageIO.read(inputStream);
        }

        if (inputImage == null) {
            throw new IllegalArgumentException("Could not read image content or unsupported image file");
        }

        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String cleanSlugName = SlugUtils.toSlug(baseName);
        if (cleanSlugName.isEmpty()) {
            cleanSlugName = "image";
        }
        String webpFileName = UUID.randomUUID().toString() + "_" + cleanSlugName + ".webp";

        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(webpFileName);
        File outputFile = filePath.toFile();

        boolean written = ImageIO.write(inputImage, "webp", outputFile);
        if (!written) {
            throw new IOException("Failed to convert and save image to WebP format");
        }

        return filePath.toString();
    }
}
