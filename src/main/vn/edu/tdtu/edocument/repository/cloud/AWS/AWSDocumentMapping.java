package vn.edu.tdtu.edocument.repository.cloud.AWS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AWSDocumentMapping {
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
        root.put("fileExtension", document.fileExtension);
        root.put("fileSizeKB", document.fileSizeKB);
        root.put("digitalSignature", document.digitalSignature);
        root.put("extractedContent", document.extractedContent);
        root.put("extractedContentHash", document.extractedContentHash);
        root.put("status", enumToString(document.status));
        putPreferenceArray(root, "applicantPreference", document.applicantPreference);
        putPreferenceArray(root, "officerPreference", document.officerPreference);

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
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

            String fileExtension = nullIfLiteralNull(textOrNull(root.get("fileExtension")));
            doc.fileExtension = fileExtension == null ? null : fileExtension.trim().toUpperCase(Locale.ROOT);

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

            List<NotificationChannelType> applicantPreference = parsePreferenceList(root.get("applicantPreference"));
            if (applicantPreference != null) {
                doc.applicantPreference = applicantPreference;
            }

            List<NotificationChannelType> officerPreference = parsePreferenceList(root.get("officerPreference"));
            if (officerPreference != null) {
                doc.officerPreference = officerPreference;
            }

            return doc;
        } catch (Exception e) {
            return null;
        }
    }

    private static String enumToString(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static void putPreferenceArray(ObjectNode root, String fieldName,
            List<NotificationChannelType> preferences) {
        if (preferences == null) {
            root.putNull(fieldName);
            return;
        }
        var array = MAPPER.createArrayNode();
        for (NotificationChannelType type : preferences) {
            if (type != null) {
                array.add(type.name());
            }
        }
        root.set(fieldName, array);
    }

    private static String normalizeEnum(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
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

    private static List<NotificationChannelType> parsePreferenceList(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        List<NotificationChannelType> result = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                NotificationChannelType type = parseEnum(NotificationChannelType.class, textOrNull(item));
                if (type != null) {
                    result.add(type);
                }
            }
            return result;
        }

        NotificationChannelType type = parseEnum(NotificationChannelType.class, textOrNull(node));
        if (type != null) {
            result.add(type);
            return result;
        }
        return null;
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
