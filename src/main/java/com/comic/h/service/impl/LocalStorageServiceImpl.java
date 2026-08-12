package com.comic.h.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.comic.h.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalStorageServiceImpl implements FileStorageService {

    @Override
    public String saveFile(byte[] fileData, String targetDirStr, String fileName) {
        if (fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("File content cannot be empty");
        }
        if (targetDirStr == null || targetDirStr.trim().isEmpty() || fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Target directory and file name must be provided");
        }

        try {
            Path dirPath = Paths.get(targetDirStr);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, fileData);
            return filePath.toString().replace('\\', '/');
        } catch (IOException e) {
            log.error("Failed to save file to path: {}/{}", targetDirStr, fileName, e);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete file at path: {}", filePath, e);
            return false;
        }
    }

    @Override
    public void deleteFiles(List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            for (String filePath : filePaths) {
                deleteFile(filePath);
            }
        }
    }

    @Override
    public boolean deleteDirectory(String dirPathStr) {
        if (dirPathStr == null || dirPathStr.trim().isEmpty()) {
            return false;
        }
        Path dirPath = Paths.get(dirPathStr);
        if (!Files.exists(dirPath)) {
            return false;
        }
        try (var stream = Files.walk(dirPath)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        } catch (IOException e) {
            log.warn("Failed to delete directory at path: {}", dirPathStr, e);
            return false;
        }
    }

    @Override
    public boolean moveDirectory(String sourceDirStr, String targetDirStr) {
        if (sourceDirStr == null || targetDirStr == null) {
            return false;
        }
        Path sourceDir = Paths.get(sourceDirStr);
        Path targetDir = Paths.get(targetDirStr);
        if (!Files.exists(sourceDir)) {
            return false;
        }
        try {
            if (targetDir.getParent() != null && !Files.exists(targetDir.getParent())) {
                Files.createDirectories(targetDir.getParent());
            }
            Files.move(sourceDir, targetDir);
            return true;
        } catch (IOException e) {
            log.warn("Failed to move directory from {} to {}", sourceDirStr, targetDirStr, e);
            return false;
        }
    }

    @Override
    public void scheduleFileCleanupOnCommit(List<String> filesToDeleteOnCommit, List<String> filesToDeleteOnRollback) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        if (filesToDeleteOnCommit != null && !filesToDeleteOnCommit.isEmpty()) {
                            deleteFiles(filesToDeleteOnCommit);
                        }
                    } else {
                        if (filesToDeleteOnRollback != null && !filesToDeleteOnRollback.isEmpty()) {
                            deleteFiles(filesToDeleteOnRollback);
                        }
                    }
                }
            });
        } else {
            if (filesToDeleteOnCommit != null && !filesToDeleteOnCommit.isEmpty()) {
                deleteFiles(filesToDeleteOnCommit);
            }
        }
    }

    @Override
    public void scheduleDirectoryCleanupOnCommit(String dirPathToDeleteOnCommit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        deleteDirectory(dirPathToDeleteOnCommit);
                    }
                }
            });
        } else {
            deleteDirectory(dirPathToDeleteOnCommit);
        }
    }
}
