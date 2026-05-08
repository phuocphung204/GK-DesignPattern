package vn.edu.tdtu.edocument.document.builder;

import java.util.UUID;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.*;

public class DocumentBuilder implements IDocumentBuilder {
    private Document _result;
    private UUID id = UUID.randomUUID();

    public DocumentBuilder() {
        _result = new Document(id);
    }

    public DocumentBuilder(Document doc) {
        _result = doc;
    }

    @Override
    public void Reset() {
        _result = new Document(_result.id);
    }

    @Override
    public void SetPersonalInfo(String name, String email, String phone) {
        _result.applicantName = name;
        _result.applicantEmail = email;
        _result.applicantPhone = phone;
    }

    @Override
    public void SetFileInfo(DocumentTypes documentType, String filePath, DocumentExtension fileExtension, long fileSizeKB, String digitalSignature) {
        _result.documentType = documentType;
        _result.filePath = filePath;
        _result.fileExtension = fileExtension;
        _result.fileSizeKB = fileSizeKB;
        _result.digitalSignature = digitalSignature;
    }

    @Override
    public void SetSubmissionInfo(String name, String email, String phone) {
        _result.officerName = name;
        _result.officerEmail = email;
        _result.officerPhone = phone;
    }

    @Override
    public Document Build() {
        return _result;
    }
}
