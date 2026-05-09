package vn.edu.tdtu.edocument.repositories.database.MongoDB;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.model.enums.DocumentTypes;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public class DocumentMapping {
    public static org.bson.Document mapDocumentToBson(Document document) {
        if (document == null) {
            return null;
        }

        return new org.bson.Document()
            .append("_id", document.id == null ? null : document.id.toString())
                .append("applicantName", document.applicantName)
                .append("applicantEmail", document.applicantEmail)
                .append("applicantPhone", document.applicantPhone)
                .append("officerName", document.officerName)
                .append("officerEmail", document.officerEmail)
                .append("officerPhone", document.officerPhone)
                .append("documentType", enumToString(document.documentType))
                .append("filePath", document.filePath)
                .append("fileExtension", enumToString(document.fileExtension))
                .append("fileSizeKB", document.fileSizeKB)
                .append("digitalSignature", document.digitalSignature)
                .append("extractedContent", document.extractedContent)
                .append("extractedContentHash", document.extractedContentHash)
                .append("status", enumToString(document.status));
    }

    public static Document mapBsonToDocument(org.bson.Document bsonDoc) {
        if (bsonDoc == null) {
            return null;
        }

        Document doc = new Document(readUuidId(bsonDoc));
        doc.applicantName = bsonDoc.getString("applicantName");
        doc.applicantEmail = bsonDoc.getString("applicantEmail");
        doc.applicantPhone = bsonDoc.getString("applicantPhone");
        doc.officerName = bsonDoc.getString("officerName");
        doc.officerEmail = bsonDoc.getString("officerEmail");
        doc.officerPhone = bsonDoc.getString("officerPhone");

        doc.documentType = parseEnum(DocumentTypes.class, bsonDoc.getString("documentType"));
        doc.filePath = bsonDoc.getString("filePath");

        doc.fileExtension = parseEnum(DocumentExtension.class, bsonDoc.getString("fileExtension"));

        Number fileSize = bsonDoc.get("fileSizeKB", Number.class);
        if (fileSize != null) {
            doc.fileSizeKB = fileSize.longValue();
        }

        doc.digitalSignature = bsonDoc.getString("digitalSignature");
        doc.extractedContent = bsonDoc.getString("extractedContent");
        doc.extractedContentHash = bsonDoc.getString("extractedContentHash");

        DocumentStatus status = parseEnum(DocumentStatus.class, bsonDoc.getString("status"));
        if (status != null) {
            doc.status = status;
        }

        return doc;
    }

    private static UUID readUuidId(org.bson.Document bsonDoc) {
        // Prefer explicit "id" field if present (some legacy docs may have both "_id" and "id").
        UUID fromIdField = parseUuidOrNull(bsonDoc.getString("id"));
        if (fromIdField != null) {
            return fromIdField;
        }

        // New docs: "_id" stored as UUID string.
        Object raw = bsonDoc.get("_id");
        if (raw == null) {
            return null;
        }
        if (raw instanceof UUID uuid) {
            return uuid;
        }
        if (raw instanceof String s) {
            return parseUuidOrNull(s);
        }
        // Legacy MongoDB default: ObjectId.
        if (raw instanceof org.bson.types.ObjectId objectId) {
            // Deterministic mapping so the same ObjectId becomes the same UUID in the app.
            String hex = objectId.toHexString();
            return UUID.nameUUIDFromBytes(hex.getBytes(StandardCharsets.UTF_8));
        }
        // If driver stored UUID as Binary, try best-effort cast via Document API.
        try {
            UUID uuid = bsonDoc.get("_id", UUID.class);
            if (uuid != null) {
                return uuid;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private static UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String enumToString(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String normalizeEnum(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, normalizeEnum(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
