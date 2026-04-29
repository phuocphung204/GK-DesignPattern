package vn.edu.tdtu.edocument.RepositoryPattern.local_storage;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.*;

public class MappingDocument {
    
    public static String mapDocumentToJson(Document document) {
        if (document == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n  \"id\": ").append(toJsonValue(document.id)).append(",");
        sb.append("\n  \"applicantName\": ").append(toJsonValue(document.applicantName)).append(",");
        sb.append("\n  \"applicantEmail\": ").append(toJsonValue(document.applicantEmail)).append(",");
        sb.append("\n  \"applicantPhone\": ").append(toJsonValue(document.applicantPhone)).append(",");
        sb.append("\n  \"officerName\": ").append(toJsonValue(document.officerName)).append(",");
        sb.append("\n  \"officerEmail\": ").append(toJsonValue(document.officerEmail)).append(",");
        sb.append("\n  \"officerPhone\": ").append(toJsonValue(document.officerPhone)).append(",");
        sb.append("\n  \"documentType\": ").append(toJsonValue(enumToString(document.documentType))).append(",");
        sb.append("\n  \"filePath\": ").append(toJsonValue(document.filePath)).append(",");
        sb.append("\n  \"fileExtension\": ").append(toJsonValue(enumToString(document.fileExtension))).append(",");
        sb.append("\n  \"fileSizeKB\": ").append(document.fileSizeKB).append(",");
        sb.append("\n  \"digitalSignature\": ").append(toJsonValue(document.digitalSignature)).append(",");
        sb.append("\n  \"extractedContent\": ").append(toJsonValue(document.extractedContent)).append(",");
        sb.append("\n  \"status\": ").append(toJsonValue(enumToString(document.status)));
        sb.append("\n}");
        return sb.toString();
    }

    public static Document mapJsonToDocument(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        Document doc = new Document();
        doc.id = nullIfLiteralNull(extractValue(json, "id"));
        doc.applicantName = nullIfLiteralNull(extractValue(json, "applicantName"));
        doc.applicantEmail = nullIfLiteralNull(extractValue(json, "applicantEmail"));
        doc.applicantPhone = nullIfLiteralNull(extractValue(json, "applicantPhone"));
        doc.officerName = nullIfLiteralNull(extractValue(json, "officerName"));
        doc.officerEmail = nullIfLiteralNull(extractValue(json, "officerEmail"));
        doc.officerPhone = nullIfLiteralNull(extractValue(json, "officerPhone"));

        String documentType = nullIfLiteralNull(extractValue(json, "documentType"));
        if (documentType != null && !documentType.isBlank()) {
            doc.documentType = DocumentTypes.valueOf(documentType);
        }

        doc.filePath = nullIfLiteralNull(extractValue(json, "filePath"));

        String fileExtension = nullIfLiteralNull(extractValue(json, "fileExtension"));
        if (fileExtension != null && !fileExtension.isBlank()) {
            doc.fileExtension = DocumentExtension.valueOf(fileExtension);
        }

        String fileSize = nullIfLiteralNull(extractValue(json, "fileSizeKB"));
        if (fileSize != null && !fileSize.isBlank()) {
            doc.fileSizeKB = Long.parseLong(fileSize);
        }

        doc.digitalSignature = nullIfLiteralNull(extractValue(json, "digitalSignature"));
        doc.extractedContent = nullIfLiteralNull(extractValue(json, "extractedContent"));

        String status = nullIfLiteralNull(extractValue(json, "status"));
        if (status != null && !status.isBlank()) {
            doc.status = DocumentStatus.valueOf(status);
        }

        return doc;
    }

    private static String enumToString(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String toJsonValue(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return null;
        }

        start += pattern.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length()) {
            return null;
        }

        if (json.startsWith("null", start)) {
            return null;
        }

        if (json.charAt(start) == '\"') {
            start++;
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < json.length(); i++) {
                char ch = json.charAt(i);
                if (escaped) {
                    sb.append(unescapeChar(ch));
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '\"') {
                    break;
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        int end = start;
        while (end < json.length() && ",}\n\r".indexOf(json.charAt(end)) == -1) {
            end++;
        }
        return json.substring(start, end).trim();
    }

    private static char unescapeChar(char ch) {
        switch (ch) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case '\\':
                return '\\';
            case '\"':
                return '\"';
            default:
                return ch;
        }
    }

    private static String nullIfLiteralNull(String value) {
        if (value == null) {
            return null;
        }
        return "null".equalsIgnoreCase(value) ? null : value;
    }
}
