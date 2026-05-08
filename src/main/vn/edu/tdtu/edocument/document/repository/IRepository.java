package vn.edu.tdtu.edocument.document.repository;

import java.util.List;
import java.util.UUID;

import vn.edu.tdtu.edocument.document.model.Document;

public interface IRepository {
    boolean ExistsByHash(String hash);
    Document GetDocumentById(UUID id);
    Document GetLatestDraftOrUploaded();
    List <Document> GetAllDocuments();
    void CreateDocument(Document doc);
    void UpdateDocument(Document doc);
    void CreateOrUpdateDocument(Document doc);
    void DeleteDocument(UUID id);
}
