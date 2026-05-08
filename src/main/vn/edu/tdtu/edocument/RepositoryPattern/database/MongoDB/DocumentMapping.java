package vn.edu.tdtu.edocument.RepositoryPattern.database.MongoDB;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.model.enums.DocumentTypes;

import java.util.Locale;

public class DocumentMapping {
    public static org.bson.Document mapDocumentToBson(Document document) {
        if (document == null) {
            return null;
        }

        return new org.bson.Document()
                .append("id", document.id)
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

        Document doc = new Document(bsonDoc.getString("id"));
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
