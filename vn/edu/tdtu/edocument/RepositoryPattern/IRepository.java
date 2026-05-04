package vn.edu.tdtu.edocument.RepositoryPattern;

import java.util.List;

import vn.edu.tdtu.edocument.model.Document;

public interface IRepository {
    boolean ExistsByHash(String hash);
    Document GetDocumentById(String id);
    Document GetLatestDraftOrUploaded();
    List <Document> GetAllDocuments();
    void CreateDocument(Document doc);
    void UpdateDocument(Document doc);
    void CreateOrUpdateDocument(Document doc);
    void DeleteDocument(String id);
}
