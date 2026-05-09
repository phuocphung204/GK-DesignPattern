package vn.edu.tdtu.edocument.repositories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileStorageHelper {
    public static final String DEFAULT_STORAGE_DIR = "server_storage";

    private FileStorageHelper() {
        // Utility class
    }

    /**
     * Keep filenames backward-compatible with existing server_storage: 8 hex chars.
     * UUID.toString() format is "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx".
     */
    public static String storageKey(UUID id) {
        if (id == null) {
            return null;
        }
        return id.toString().substring(0, 8);
    }

    public static Path buildStoredFilePath(String storageDirPath, UUID id, Path sourcePath) {
        String fileName = sourcePath.getFileName().toString();
        String prefix = storageKey(id) + "_";
        String targetFileName = fileName.startsWith(prefix) ? fileName : (prefix + fileName);
        return Paths.get(storageDirPath + File.separator + targetFileName);
    }

    public static String copyToDefaultStorage(UUID id, String sourceFilePath) throws IOException {
        return copyToStorageDir(DEFAULT_STORAGE_DIR, id, sourceFilePath);
    }

    public static String copyToStorageDir(String storageDirPath, UUID id, String sourceFilePath) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("Document id is null");
        }
        if (sourceFilePath == null || sourceFilePath.isBlank()) {
            throw new IllegalArgumentException("Source file path is empty");
        }

        Path sourcePath = Paths.get(sourceFilePath);
        if (!Files.exists(sourcePath)) {
            throw new IOException("Source file does not exist: " + sourceFilePath);
        }

        Files.createDirectories(Paths.get(storageDirPath));

        Path targetPath = buildStoredFilePath(storageDirPath, id, sourcePath);
        if (!sourcePath.normalize().toAbsolutePath().equals(targetPath.normalize().toAbsolutePath())) {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetPath.toString();
    }
}
