package vn.edu.tdtu.edocument.document.builder;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.*;

public interface IDocumentBuilder {
    void Reset();
    void SetPersonalInfo(String name, String email, String phone);
    void SetFileInfo(DocumentTypes documentType, String filePath, String fileExtension, long fileSizeKB, String digitalSignature);
    void SetSubmissionInfo(String name, String email, String phone);
    Document Build();
}