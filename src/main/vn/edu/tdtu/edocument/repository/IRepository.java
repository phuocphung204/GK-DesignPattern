package vn.edu.tdtu.edocument.repository;

import java.util.List;
import java.util.UUID;

import vn.edu.tdtu.edocument.document.model.Document;

public interface IRepository {
    boolean ExistsByHash(String hash);

    Document GetDocumentById(UUID id);

    Document GetLatestDraft();

    List<Document> GetAllDocuments();

    void CreateDocument(Document doc);

    void UpdateDocument(Document doc);

    void DeleteDocument(UUID id);
}
