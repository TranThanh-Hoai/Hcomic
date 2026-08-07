package com.comic.h.service;

import java.util.List;

public interface FileStorageService {

    String saveFile(byte[] fileData, String targetDirStr, String fileName);

    boolean deleteFile(String filePath);

    void deleteFiles(List<String> filePaths);

    boolean deleteDirectory(String dirPathStr);

    boolean moveDirectory(String sourceDirStr, String targetDirStr);

    void scheduleFileCleanupOnCommit(List<String> filesToDeleteOnCommit, List<String> filesToDeleteOnRollback);

    void scheduleDirectoryCleanupOnCommit(String dirPathToDeleteOnCommit);
}
