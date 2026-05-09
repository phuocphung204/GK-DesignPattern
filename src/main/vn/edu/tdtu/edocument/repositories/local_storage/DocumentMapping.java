package vn.edu.tdtu.edocument.repositories.local_storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.*;

import java.util.Locale;
import java.util.UUID;

public class DocumentMapping {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public static String mapDocumentToJson(Document document) {
        if (document == null) {
            return null;
        }

        ObjectNode root = MAPPER.createObjectNode();
        root.put("id", document.id == null ? null : document.id.toString());
        root.put("applicantName", document.applicantName);
        root.put("applicantEmail", document.applicantEmail);
        root.put("applicantPhone", document.applicantPhone);
        root.put("officerName", document.officerName);
        root.put("officerEmail", document.officerEmail);
        root.put("officerPhone", document.officerPhone);
        root.put("documentType", enumToString(document.documentType));
        root.put("filePath", document.filePath);
        root.put("fileExtension", enumToString(document.fileExtension));
        root.put("fileSizeKB", document.fileSizeKB);
        root.put("digitalSignature", document.digitalSignature);
        root.put("extractedContent", document.extractedContent);
        root.put("extractedContentHash", document.extractedContentHash);
        root.put("status", enumToString(document.status));

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            // Keep existing behavior of returning a best-effort String (callers already handle null/empty).
            return null;
        }
    }

    public static Document mapJsonToDocument(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            JsonNode root = MAPPER.readTree(json);
            UUID id = parseUuidOrNull(textOrNull(root.get("id")));
            Document doc = new Document(id);
            doc.applicantName = nullIfLiteralNull(textOrNull(root.get("applicantName")));
            doc.applicantEmail = nullIfLiteralNull(textOrNull(root.get("applicantEmail")));
            doc.applicantPhone = nullIfLiteralNull(textOrNull(root.get("applicantPhone")));
            doc.officerName = nullIfLiteralNull(textOrNull(root.get("officerName")));
            doc.officerEmail = nullIfLiteralNull(textOrNull(root.get("officerEmail")));
            doc.officerPhone = nullIfLiteralNull(textOrNull(root.get("officerPhone")));

            doc.documentType = parseEnum(DocumentTypes.class, textOrNull(root.get("documentType")));
            doc.filePath = nullIfLiteralNull(textOrNull(root.get("filePath")));
            doc.fileExtension = parseEnum(DocumentExtension.class, textOrNull(root.get("fileExtension")));

            JsonNode fileSizeNode = root.get("fileSizeKB");
            if (fileSizeNode != null && !fileSizeNode.isNull() && !fileSizeNode.isMissingNode()) {
                if (fileSizeNode.isNumber()) {
                    doc.fileSizeKB = fileSizeNode.asLong();
                } else {
                    String fileSizeText = nullIfLiteralNull(textOrNull(fileSizeNode));
                    if (fileSizeText != null && !fileSizeText.isBlank()) {
                        doc.fileSizeKB = Long.parseLong(fileSizeText);
                    }
                }
            }

            doc.digitalSignature = nullIfLiteralNull(textOrNull(root.get("digitalSignature")));
            doc.extractedContent = nullIfLiteralNull(textOrNull(root.get("extractedContent")));
            doc.extractedContentHash = nullIfLiteralNull(textOrNull(root.get("extractedContentHash")));

            DocumentStatus status = parseEnum(DocumentStatus.class, textOrNull(root.get("status")));
            if (status != null) {
                doc.status = status;
            }

            return doc;
        } catch (Exception e) {
            // Invalid JSON or unexpected values -> return null to keep callers safe.
            return null;
        }
    }

    private static String enumToString(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String normalizeEnum(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        // For non-strings (numbers/booleans), keep a string representation (for legacy fields).
        return node.asText();
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        value = nullIfLiteralNull(value);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, normalizeEnum(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String nullIfLiteralNull(String value) {
        if (value == null) {
            return null;
        }
        return "null".equalsIgnoreCase(value) ? null : value;
    }

    private static UUID parseUuidOrNull(String value) {
        value = nullIfLiteralNull(value);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
