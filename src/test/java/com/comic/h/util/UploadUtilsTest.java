package com.comic.h.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class UploadUtilsTest {

    @TempDir
    Path tempDir;

    private MockMultipartFile mockFile1;
    private MockMultipartFile mockFile2;

    @BeforeEach
    public void setUp() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] bytes = os.toByteArray();

        mockFile1 = new MockMultipartFile("file", "test1.png", "image/png", bytes);
        mockFile2 = new MockMultipartFile("file", "test2.png", "image/png", bytes);
    }

    @Test
    public void testSaveFile_CustomDirAndFileName() throws IOException {
        Path comicDir = tempDir.resolve("solo-leveling");
        String savedPathStr = UploadUtils.saveFile(mockFile1, comicDir, "solo-leveling-cover.webp");

        assertNotNull(savedPathStr);
        Path savedPath = Path.of(savedPathStr);
        assertTrue(Files.exists(savedPath));
        assertEquals("solo-leveling-cover.webp", savedPath.getFileName().toString());
    }

    @Test
    public void testSaveFiles_CustomDirAndPrefixSequence() throws IOException {
        Path chapterDir = tempDir.resolve("solo-leveling").resolve("solo-leveling-chapter-1-12345");
        List<MultipartFile> files = List.of(mockFile1, mockFile2);

        List<String> savedPaths = UploadUtils.saveFiles(files, chapterDir, "solo-leveling");

        assertNotNull(savedPaths);
        assertEquals(2, savedPaths.size());

        Path path1 = Path.of(savedPaths.get(0));
        Path path2 = Path.of(savedPaths.get(1));

        assertTrue(Files.exists(path1));
        assertTrue(Files.exists(path2));

        assertEquals("solo-leveling-img-001.webp", path1.getFileName().toString());
        assertEquals("solo-leveling-img-002.webp", path2.getFileName().toString());
    }

    @Test
    public void testDeleteDirectory_RecursiveDeletion() throws IOException {
        Path comicDir = tempDir.resolve("to-delete");
        Path subDir = comicDir.resolve("sub-folder");
        Files.createDirectories(subDir);

        Path dummyFile = subDir.resolve("dummy.txt");
        Files.writeString(dummyFile, "hello");

        assertTrue(Files.exists(dummyFile));

        boolean deleted = UploadUtils.deleteDirectory(comicDir);
        assertTrue(deleted);
        assertFalse(Files.exists(comicDir));
    }
}
