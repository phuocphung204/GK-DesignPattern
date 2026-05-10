package src.test.vn.edu.tdtu.edocument.document.repository.local_storage;

import org.junit.jupiter.api.Test;
import vn.edu.tdtu.edocument.document.repository.local_storage.DocumentMapping;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentMappingTest {

    @Test
    void roundTrip_preservesFieldsAndEscapes() {
        Document doc = new Document(java.util.UUID.randomUUID());
        doc.applicantName = "Nguyen Van A";
        doc.applicantEmail = "a@example.com";
        doc.applicantPhone = "0123456789";
        doc.officerName = "Officer 1";
        doc.officerEmail = "officer@example.com";
        doc.officerPhone = "0987654321";
        doc.documentType = DocumentTypes.BAO_CAO;
        doc.filePath = "C:\\temp\\a.pdf";
        doc.fileExtension = "PDF";
        doc.fileSizeKB = 42;
        doc.digitalSignature = "sig";
        doc.extractedContent = "line1\n\"quoted\" \\ backslash\tend";
        doc.extractedContentHash = "hash123";
        doc.status = DocumentStatus.DA_TAI_FILE;

        String json = DocumentMapping.mapDocumentToJson(doc);
        assertNotNull(json);

        Document parsed = DocumentMapping.mapJsonToDocument(json);
        assertNotNull(parsed);

        assertEquals(doc.id, parsed.id);
        assertEquals(doc.applicantName, parsed.applicantName);
        assertEquals(doc.applicantEmail, parsed.applicantEmail);
        assertEquals(doc.applicantPhone, parsed.applicantPhone);
        assertEquals(doc.officerName, parsed.officerName);
        assertEquals(doc.officerEmail, parsed.officerEmail);
        assertEquals(doc.officerPhone, parsed.officerPhone);
        assertEquals(doc.documentType, parsed.documentType);
        assertEquals(doc.filePath, parsed.filePath);
        assertEquals(doc.fileExtension, parsed.fileExtension);
        assertEquals(doc.fileSizeKB, parsed.fileSizeKB);
        assertEquals(doc.digitalSignature, parsed.digitalSignature);
        assertEquals(doc.extractedContent, parsed.extractedContent);
        assertEquals(doc.extractedContentHash, parsed.extractedContentHash);
        assertEquals(doc.status, parsed.status);

        // Not mapped fields keep defaults.
        assertNotNull(parsed.userPreference);
    }

    @Test
    void mapJsonToDocument_parsesEnumsCaseInsensitive() {
        String id = "00000000-0000-0000-0000-000000000001";
        String json = "{\n" +
            "  \"id\": \"" + id + "\",\n" +
                "  \"documentType\": \"bao_cao\",\n" +
                "  \"fileExtension\": \"pdf\",\n" +
                "  \"status\": \"da_tiep_nhan\",\n" +
                "  \"fileSizeKB\": 10\n" +
                "}";

        Document parsed = DocumentMapping.mapJsonToDocument(json);
        assertNotNull(parsed);
        assertEquals(java.util.UUID.fromString(id), parsed.id);
        assertEquals(DocumentTypes.BAO_CAO, parsed.documentType);
        assertEquals("PDF", parsed.fileExtension);
        assertEquals(DocumentStatus.DA_TIEP_NHAN, parsed.status);
        assertEquals(10, parsed.fileSizeKB);
    }

    @Test
    void mapJsonToDocument_convertsLiteralNullStringToNull() {
        String id = "00000000-0000-0000-0000-000000000002";
        String json = "{\n" +
            "  \"id\": \"" + id + "\",\n" +
                "  \"applicantName\": \"null\",\n" +
                "  \"officerEmail\": null\n" +
                "}";

        Document parsed = DocumentMapping.mapJsonToDocument(json);
        assertNotNull(parsed);
        assertNull(parsed.applicantName);
        assertNull(parsed.officerEmail);
    }
}
