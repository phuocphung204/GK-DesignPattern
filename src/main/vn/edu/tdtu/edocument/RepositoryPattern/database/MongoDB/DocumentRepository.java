package vn.edu.tdtu.edocument.RepositoryPattern.database.MongoDB;

import com.mongodb.client.MongoCollection;
import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.model.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.or;

public class DocumentRepository implements IRepository {

    private final MongoCollection<org.bson.Document> collection;

    public DocumentRepository(MongoDBConfiguration config) {
        this.collection = config.getCollection("documents");
    }

    @Override
    public boolean ExistsByHash(String hash) {
        var filter = new org.bson.Document("extractedContentHash", hash);
        var doc = collection.find(filter).first();
        return doc != null;
    }

    @Override
    public Document GetDocumentById(String id) {
        var filter = new org.bson.Document("id", id);
        var doc = collection.find(filter).first();
        if (doc == null) {
            return null;
        }
        return DocumentMapping.mapBsonToDocument(doc);
    }

    @Override
    public Document GetLatestDraftOrUploaded() {
        var doc = collection.find(or(
                new org.bson.Document("status", "DRAFT"),
                new org.bson.Document("status", "UPLOADED")
        )).first();
        if (doc == null) {
            return null;
        }
        return DocumentMapping.mapBsonToDocument(doc);
    }

    @Override
    public List<Document> GetAllDocuments() {
        List<org.bson.Document> bsonDocs = collection.find().into(new ArrayList<>());
        return bsonDocs.stream().map(DocumentMapping::mapBsonToDocument).collect(Collectors.toList());
    }

    @Override
    public void CreateDocument(Document doc) {
        var bsonDoc = DocumentMapping.mapDocumentToBson(doc);
        collection.insertOne(bsonDoc);
    }

    @Override
    public void UpdateDocument(Document doc) {
        var filter = new org.bson.Document("id", doc.id);
        var bsonDoc = DocumentMapping.mapDocumentToBson(doc);
        bsonDoc.remove("id");
        var update = new org.bson.Document("$set", bsonDoc);
        collection.updateOne(filter, update);
    }

    public boolean existById(String id) {
        var filter = new org.bson.Document("id", id);
        var doc = collection.find(filter).first();
        return doc != null;
    }

    @Override
    public void CreateOrUpdateDocument(Document doc) {
        if (doc == null || doc.id == null || doc.id.isBlank()) {
            System.out.println("[LỖI HỆ THỐNG] Hồ sơ không hợp lệ.");
            return;
        }
        if (!existById(doc.id)) {
            CreateDocument(doc);
        } else {
            UpdateDocument(doc);
        }
    }

    @Override
    public void DeleteDocument(String id) {
        var filter = new org.bson.Document("id", id);
        collection.deleteOne(filter);
    }
}
