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

import java.util.ArrayList;
import java.util.List;

public class JsonStorage implements IRepository {
    private static final String STORAGE_DIR = "server_storage";
    private static final JsonStorage _instance = new JsonStorage(); // Singleton instance EAGER initialization

    private JsonStorage() {
        // Private constructor to prevent instantiation
    }
    
    public static JsonStorage getInstance() {
        return _instance;
    }

    public Document GetDocumentById(String id) {
        // Implementation to read JSON file and return Document object by ID
        String storageDirPath = STORAGE_DIR;
        File dataFile = new File(storageDirPath + File.separator + id + "_data.json");
        if (dataFile.exists()) {
            try {
                String json = new String(Files.readAllBytes(dataFile.toPath()));
                return MappingDocument.mapJsonToDocument(json); // Convert JSON string back to Document object
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
                        Document doc = MappingDocument.mapJsonToDocument(json); // Convert JSON string back to Document object
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

    @Override
    public void CreateDocument(Document doc) {
        String storageDirPath = STORAGE_DIR;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            Path sourcePath = Paths.get(doc.filePath);
            Path targetPath = Paths.get(storageDirPath + File.separator + doc.id + "_" + sourcePath.getFileName().toString());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            doc.filePath = targetPath.toString();
            String json = MappingDocument.mapDocumentToJson(doc); // Convert Document object to JSON string

            File dataFile = new File(storageDirPath + File.separator + doc.id + "_data.json");
            FileWriter writer = new FileWriter(dataFile);
            writer.write(json == null ? "" : json);
            writer.close();

        } catch (IOException e) {
            System.out.println("[LỖI HỆ THỐNG] Lỗi khi lưu trữ vật lý: " + e.getMessage());
        }
    }

    @Override
    public void UpdateDocument(Document doc) {
        String storageDirPath = STORAGE_DIR;

        try {
            if (doc.filePath != null && !doc.filePath.isBlank()) {
                Path sourcePath = Paths.get(doc.filePath);
                Path targetPath = Paths.get(storageDirPath + File.separator + doc.id + "_" + sourcePath.getFileName().toString());
                if (!sourcePath.equals(targetPath)) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    doc.filePath = targetPath.toString();
                }
            }

            String json = MappingDocument.mapDocumentToJson(doc); // Convert Document object to JSON string
            File dataFile = new File(storageDirPath + File.separator + doc.id + "_data.json");
            FileWriter writer = new FileWriter(dataFile);
            writer.write(json == null ? "" : json);
            writer.close();

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
