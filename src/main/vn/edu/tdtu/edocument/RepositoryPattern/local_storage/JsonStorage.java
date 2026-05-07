package vn.edu.tdtu.edocument.RepositoryPattern.local_storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;

import java.util.ArrayList;
import java.util.List;

public class JsonStorage implements IRepository {
    private static final String STORAGE_DIR = "server_storage";
    private static final JsonStorage _instance = new JsonStorage(); // Singleton instance EAGER initialization

    private static Path buildStoredFilePath(String storageDirPath, String id, Path sourcePath) {
        // Avoid duplicating the prefix if the file name already starts with "<id>_".
        String fileName = sourcePath.getFileName().toString();
        String prefix = id + "_";
        String targetFileName = fileName.startsWith(prefix) ? fileName : (prefix + fileName);
        return Paths.get(storageDirPath + File.separator + targetFileName);
    }

    private JsonStorage() {
        // Private constructor to prevent instantiation
    }
    
    public static JsonStorage getInstance() {
        return _instance;
    }

    public boolean ExistsById(String id) {
        if (id == null || id.isBlank()) 
            return false;
        String storageDirPath = STORAGE_DIR;
        File dataFile = new File(storageDirPath + File.separator + id + "_data.json");
        return dataFile.exists();
    }

    @Override
    public void CreateOrUpdateDocument(Document doc) {
        if (doc == null || doc.id == null || doc.id.isBlank()) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }
        if (ExistsById(doc.id)) {
            UpdateDocument(doc);
        } else {
            CreateDocument(doc);
        }
    }

    public boolean ExistsByHash(String hash) {
        if (hash == null || hash.isBlank()) 
            return false;
        List<Document> allDocs = GetAllDocuments();
        return allDocs.stream()
            .anyMatch(doc -> hash.equals(doc.extractedContentHash) && doc.status != DocumentStatus.DA_TAI_FILE);
    }

    public Document GetDocumentById(String id) {
        // Implementation to read JSON file and return Document object by ID
        String storageDirPath = STORAGE_DIR;
        File dataFile = new File(storageDirPath + File.separator + id + "_data.json");
        if (dataFile.exists()) {
            try {
                String json = new String(Files.readAllBytes(dataFile.toPath()));
                return DocumentMapping.mapJsonToDocument(json); // Convert JSON string back to Document object
            } catch (IOException e) {
                System.out.println("[LỖI HỆ THỐNG] Lỗi khi đọc dữ liệu: " + e.getMessage());
            }
        } else {
            System.out.println("[THÔNG BÁO] Không tìm thấy hồ sơ với ID: " + id);
        }
        return null; // Placeholder
    }

    public List<Document> GetAllDocuments() {
        // Implementation to read all JSON files in the storage directory and return a list of Document objects
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        List<Document> documents = new ArrayList<>();

        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles((dir, name) -> name.endsWith("_data.json"));
            if (files != null) {
                for (File dataFile : files) {
                    try {
                        String json = new String(Files.readAllBytes(dataFile.toPath()));
                        Document doc = DocumentMapping.mapJsonToDocument(json); // Convert JSON string back to Document object
                        if (doc != null) {
                            documents.add(doc);
                        }
                    } catch (IOException e) {
                        System.out.println("[LỖI HỆ THỐNG] Lỗi khi đọc dữ liệu: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("[THÔNG BÁO] Thư mục lưu trữ không tồn tại.");
        }
        return documents;
    }

    public Document GetLatestDraftOrUploaded() {
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists() || !storageDir.isDirectory()) {
            return null;
        }

        File[] files = storageDir.listFiles((dir, name) -> name.endsWith("_data.json"));
        if (files == null || files.length == 0) {
            return null;
        }

        Document latestDoc = null;
        long latestModified = -1;

        for (File dataFile : files) {
            try {
                String json = new String(Files.readAllBytes(dataFile.toPath()));
                Document doc = DocumentMapping.mapJsonToDocument(json);
                if (doc == null) {
                    continue;
                }
                if (doc.status != DocumentStatus.BAN_NHAP && doc.status != DocumentStatus.DA_TAI_FILE) {
                    continue;
                }
                long modified = dataFile.lastModified();
                if (modified > latestModified) {
                    latestModified = modified;
                    latestDoc = doc;
                }
            } catch (IOException e) {
                System.out.println("[LỖI HỆ THỐNG] Lỗi khi đọc dữ liệu: " + e.getMessage());
            }
        }

        return latestDoc;
    }

    @Override
    public void CreateDocument(Document doc) {
        if (doc == null || doc.id == null || doc.id.isBlank()) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }

        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            if (doc.filePath != null && !doc.filePath.isBlank()) {
                Path sourcePath = Paths.get(doc.filePath);
                Path targetPath = buildStoredFilePath(storageDirPath, doc.id, sourcePath);
                if (!sourcePath.normalize().toAbsolutePath().equals(targetPath.normalize().toAbsolutePath())) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                doc.filePath = targetPath.toString();
            }
            String json = DocumentMapping.mapDocumentToJson(doc); // Convert Document object to JSON string

            File dataFile = new File(storageDirPath + File.separator + doc.id + "_data.json");
            try (FileWriter writer = new FileWriter(dataFile)) {
                writer.write(json == null ? "" : json);
            }

        } catch (IOException e) {
            System.out.println("[LỖI HỆ THỐNG] Lỗi khi lưu trữ vật lý: " + e.getMessage());
        }
    }

    @Override
    public void UpdateDocument(Document doc) {
        if (doc == null || doc.id == null || doc.id.isBlank()) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }

        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            if (doc.filePath != null && !doc.filePath.isBlank()) {
                Path sourcePath = Paths.get(doc.filePath);
                Path targetPath = buildStoredFilePath(storageDirPath, doc.id, sourcePath);
                if (!sourcePath.normalize().toAbsolutePath().equals(targetPath.normalize().toAbsolutePath())) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                doc.filePath = targetPath.toString();
            }

            String json = DocumentMapping.mapDocumentToJson(doc); // Convert Document object to JSON string
            File dataFile = new File(storageDirPath + File.separator + doc.id + "_data.json");
            try (FileWriter writer = new FileWriter(dataFile)) {
                writer.write(json == null ? "" : json);
            }

        } catch (IOException e) {
            System.out.println("[LỖI HỆ THỐNG] Lỗi khi lưu trữ vật lý: " + e.getMessage());
        }
    }

    @Override
    public void DeleteDocument(String id) {
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles((dir, name) -> name.startsWith(id + "_"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}
