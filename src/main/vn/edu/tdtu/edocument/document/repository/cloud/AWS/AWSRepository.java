package vn.edu.tdtu.edocument.document.repository.cloud.AWS;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.repository.FileStorageHelper;
import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.repository.RepositoryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fake AWS repository.
 *
 * This class simulates persisting documents to AWS (e.g., S3/DynamoDB) by writing
 * JSON metadata to a dedicated folder under server_storage.
 */
public class AWSRepository implements IRepository {
    private static final AWSRepository INSTANCE = new AWSRepository(AWSConfiguration.getInstance());

    private final String storageDir;

    private AWSRepository(AWSConfiguration config) {
        // Fake "bucket" folder name to keep data separate from other repositories.
        String bucket = safePathSegment(config == null ? null : config.getBucketName());
        String base = "aws_storage";
        this.storageDir = (bucket == null || bucket.isBlank()) ? base : (base + File.separator + bucket);
    }

    public static AWSRepository getInstance() {
        return INSTANCE;
    }

    private String dataFileName(UUID id) {
        return FileStorageHelper.storageKey(id) + "_data.json";
    }

    private File dataFile(UUID id) {
        return new File(storageDir + File.separator + dataFileName(id));
    }

    private static String safePathSegment(String segment) {
        if (segment == null) {
            return null;
        }
        // Prevent accidental path traversal; keep it simple for the fake implementation.
        return segment.replace("/", "_").replace("\\\\", "_").trim();
    }

    private void ensureStorageDir() {
        File dir = new File(storageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public boolean ExistsByHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        List<Document> allDocs = GetAllDocuments();
        return allDocs.stream().anyMatch(doc -> doc != null
                && doc.extractedContentHash != null
                && hash.equals(doc.extractedContentHash));
    }

    @Override
    public Document GetDocumentById(UUID id) {
        File file = dataFile(id);
        if (!file.exists()) {
            return null;
        }
        try {
            String json = Files.readString(file.toPath());
            return AWSDocumentMapping.mapJsonToDocument(json);
        } catch (IOException e) {
            throw new RepositoryException("Thất bại khi đọc hồ sơ với ID: " + id + " từ lưu trữ AWS", e);
        }
    }

    @Override
    public Document GetLatestDraft() {
        File dir = new File(storageDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith("_data.json"));
        if (files == null || files.length == 0) {
            return null;
        }

        Document latestDoc = null;
        long latestModified = -1;

        for (File dataFile : files) {
            try {
                String json = Files.readString(dataFile.toPath());
                Document doc = AWSDocumentMapping.mapJsonToDocument(json);
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
                throw new RepositoryException("Thất bại khi đọc dữ liệu (AWS fake): " + e.getMessage(), e);
            }
        }

        return latestDoc;
    }

    @Override
    public List<Document> GetAllDocuments() {
        File dir = new File(storageDir);
        List<Document> documents = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            return documents;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith("_data.json"));
        if (files == null) {
            return documents;
        }

        for (File dataFile : files) {
            try {
                String json = Files.readString(dataFile.toPath());
                Document doc = AWSDocumentMapping.mapJsonToDocument(json);
                if (doc != null) {
                    documents.add(doc);
                }
            } catch (IOException e) {
                throw new RepositoryException("Thất bại khi đọc dữ liệu (AWS fake): " + e.getMessage(), e);
            }
        }

        return documents;
    }

    @Override
    public void CreateDocument(Document doc) {
        if (doc == null || doc.id == null) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }

        ensureStorageDir();

        try {
            String json = AWSDocumentMapping.mapDocumentToJson(doc);
            File target = dataFile(doc.id);
            try (FileWriter writer = new FileWriter(target)) {
                writer.write(json == null ? "" : json);
            }
        } catch (IOException e) {
            throw new RepositoryException("Thất bại khi lưu trữ (AWS fake): " + e.getMessage(), e);
        }
    }

    @Override
    public void UpdateDocument(Document doc) {
        // For the fake implementation, update is the same as write.
        CreateDocument(doc);
    }

    @Override
    public void DeleteDocument(UUID id) {
        if (id == null) {
            return;
        }
        File file = dataFile(id);
        if (file.exists()) {
            file.delete();
        }
    }

}
