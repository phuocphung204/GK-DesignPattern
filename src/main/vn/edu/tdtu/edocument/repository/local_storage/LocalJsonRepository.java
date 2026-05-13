package vn.edu.tdtu.edocument.repository.local_storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.repository.RepositoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalJsonRepository implements IRepository {
    private static final String STORAGE_DIR = "server_storage";
    private static final LocalJsonRepository _instance = new LocalJsonRepository(); // Singleton instance EAGER
                                                                                    // initialization

    private static String storageKey(UUID id) {
        if (id == null) {
            return null;
        }
        // Keep filenames backward-compatible with existing server_storage: 8 hex chars.
        // UUID.toString() format is "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx".
        return id.toString().substring(0, 8);
    }

    private LocalJsonRepository() {
        // Private constructor to prevent instantiation
    }

    public static LocalJsonRepository getInstance() {
        return _instance;
    }

    @Override
    public boolean ExistsByHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        List<Document> allDocs = GetAllDocuments();
        return allDocs.stream().anyMatch(
                doc -> doc != null && doc.extractedContentHash != null && hash.equals(doc.extractedContentHash));
    }

    @Override
    public Document GetDocumentById(UUID id) {
        // Implementation to read JSON file and return Document object by ID
        String storageDirPath = STORAGE_DIR;
        File dataFile = new File(storageDirPath + File.separator + storageKey(id) + "_data.json");
        if (dataFile.exists()) {
            try {
                String json = new String(Files.readAllBytes(dataFile.toPath()));
                return LocalJsonDocumentMapping.mapJsonToDocument(json); // Convert JSON string back to Document object
            } catch (IOException e) {
                throw new RepositoryException("Thất bại khi đọc hồ sơ với ID: " + id + " từ lưu trữ JSON", e);
            }
        } else {
            System.out.println("[THÔNG BÁO] Không tìm thấy hồ sơ với ID: " + id);
        }
        return null; // Placeholder
    }

    @Override
    public List<Document> GetAllDocuments() {
        // Implementation to read all JSON files in the storage directory and return a
        // list of Document objects
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        List<Document> documents = new ArrayList<>();

        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles((dir, name) -> name.endsWith("_data.json"));
            if (files != null) {
                for (File dataFile : files) {
                    try {
                        String json = new String(Files.readAllBytes(dataFile.toPath()));
                        Document doc = LocalJsonDocumentMapping.mapJsonToDocument(json); // Convert JSON string back to
                                                                                         // Document object
                        if (doc != null) {
                            documents.add(doc);
                        }
                    } catch (IOException ex) {
                        throw new RepositoryException("Thất bại khi đọc hồ sơ từ tệp: " + dataFile.getName(), ex);
                    }
                }
            }
        } else {
            System.out.println("[THÔNG BÁO] Thư mục lưu trữ không tồn tại.");
        }
        return documents;
    }

    @Override
    public Document GetLatestDraft() {
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
                Document doc = LocalJsonDocumentMapping.mapJsonToDocument(json);
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
                throw new RepositoryException("Thất bại khi đọc dữ liệu (JSON): " + e.getMessage(), e);
            }
        }

        return latestDoc;
    }

    @Override
    public void CreateDocument(Document doc) {
        if (doc == null || doc.id == null) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }

        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            String json = LocalJsonDocumentMapping.mapDocumentToJson(doc); // Convert Document object to JSON string

            File dataFile = new File(storageDirPath + File.separator + storageKey(doc.id) + "_data.json");
            try (FileWriter writer = new FileWriter(dataFile)) {
                writer.write(json == null ? "" : json);
            }

        } catch (IOException ex) {
            throw new RepositoryException("Thất bại khi tạo hồ sơ với ID: " + doc.id + " trong lưu trữ JSON", ex);
        }
    }

    @Override
    public void UpdateDocument(Document doc) {
        if (doc == null || doc.id == null) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }

        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            String json = LocalJsonDocumentMapping.mapDocumentToJson(doc); // Convert Document object to JSON string
            File dataFile = new File(storageDirPath + File.separator + storageKey(doc.id) + "_data.json");
            try (FileWriter writer = new FileWriter(dataFile)) {
                writer.write(json == null ? "" : json);
            }

        } catch (IOException ex) {
            throw new RepositoryException("Thất bại khi cập nhật hồ sơ với ID: " + doc.id + " trong lưu trữ JSON", ex);
        }
    }

    @Override
    public void DeleteDocument(UUID id) {
        if (id == null) {
            return;
        }
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (storageDir.exists() && storageDir.isDirectory()) {
            String prefix = storageKey(id) + "_";
            File[] files = storageDir.listFiles((dir, name) -> name.startsWith(prefix));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}
