package vn.edu.tdtu.edocument.RepositoryPattern;

import java.util.List;

import vn.edu.tdtu.edocument.model.Document;

public interface IRepository {
    Document GetDocumentById(String id);
    List <Document> GetAllDocuments();
    void CreateDocument(Document doc);
    void UpdateDocument(Document doc);
    void DeleteDocument(String id);
}
