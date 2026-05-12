package vn.edu.tdtu.edocument.document.builder;

import java.util.List;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;

public interface IDocumentBuilder {
    void Reset();
    void SetPersonalInfo(String name, String email, String phone, List<NotificationChannelType> applicantPreference);
    void SetFileInfo(DocumentTypes documentType, String filePath, String fileExtension, long fileSizeKB, String digitalSignature);
    void SetSubmissionInfo(String name, String email, String phone, List<NotificationChannelType> officerPreference);
    Document Build();
}