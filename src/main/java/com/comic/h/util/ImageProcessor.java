package com.comic.h.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageProcessor {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    public boolean isAllowedImageExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    public byte[] convertToWebp(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (!isAllowedImageExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid image file format. Allowed formats: " + ALLOWED_EXTENSIONS);
        }

        BufferedImage inputImage;
        try (InputStream inputStream = file.getInputStream()) {
            inputImage = ImageIO.read(inputStream);
        }

        if (inputImage == null) {
            throw new IllegalArgumentException("Could not read image content or unsupported image format");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean written = ImageIO.write(inputImage, "webp", baos);
        if (!written) {
            throw new IOException("Failed to convert image to WebP format");
        }

        return baos.toByteArray();
    }
}
