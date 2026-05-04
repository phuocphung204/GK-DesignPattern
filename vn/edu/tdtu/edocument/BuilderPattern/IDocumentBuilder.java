package vn.edu.tdtu.edocument.BuilderPattern;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.*;

public interface IDocumentBuilder {
    void Reset();
    void SetPersonalInfo(String name, String email, String phone);
    void SetFileInfo(DocumentTypes documentType, String filePath, DocumentExtension fileExtension, long fileSizeKB, String digitalSignature);
    void SetSubmissionInfo(String name, String email, String phone);
    Document Build();
}