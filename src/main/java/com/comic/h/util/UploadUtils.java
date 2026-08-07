package com.comic.h.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        Path dirPath = Paths.get(uploadDir);
        String originalFilename = file != null ? file.getOriginalFilename() : null;
        String baseName = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
                : "image";
        String cleanSlugName = SlugUtils.toSlug(baseName);
        if (cleanSlugName.isEmpty()) {
            cleanSlugName = "image";
        }
        String webpFileName = UUID.randomUUID().toString() + "_" + cleanSlugName + ".webp";
        return saveFile(file, dirPath, webpFileName);
    }

    public static void createDirectoryIfNotExists(Path dirPath) throws IOException {
        if (dirPath != null && !Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    public static void createDirectoryIfNotExists(String dirPathStr) throws IOException {
        if (dirPathStr != null && !dirPathStr.trim().isEmpty()) {
            createDirectoryIfNotExists(Paths.get(dirPathStr));
        }
    }

    public static String saveFile(MultipartFile file, Path dirPath, String fileName) throws IOException {
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

        createDirectoryIfNotExists(dirPath);

        Path filePath = dirPath.resolve(fileName);
        File outputFile = filePath.toFile();

        boolean written = ImageIO.write(inputImage, "webp", outputFile);
        if (!written) {
            throw new IOException("Failed to convert and save image to WebP format");
        }

        return filePath.toString();
    }


    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public static void deleteFiles(List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            for (String filePath : filePaths) {
                deleteFile(filePath);
            }
        }
    }

    public static boolean deleteDirectory(Path dirPath) {
        if (dirPath == null || !Files.exists(dirPath)) {
            return false;
        }
        try (var stream = Files.walk(dirPath)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteDirectory(String dirPathStr) {
        if (dirPathStr == null || dirPathStr.trim().isEmpty()) {
            return false;
        }
        return deleteDirectory(Paths.get(dirPathStr));
    }
}
