package vn.edu.tdtu.edocument.repository.database.MongoDB;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.repository.RepositoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class MongoDBDocumentRepository implements IRepository {

    private final MongoCollection<org.bson.Document> collection;

    public MongoDBDocumentRepository(MongoDBConfiguration config) {
        this.collection = config.getCollection("documents");
    }

    @Override
    public boolean ExistsByHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        var filter = new org.bson.Document("extractedContentHash", hash);
        var doc = collection.find(filter).first();
        return doc != null;
    }

    @Override
    public Document GetDocumentById(UUID id) {
        var filter = new org.bson.Document("_id", id);
        var doc = collection.find(filter).first();
        if (doc == null) {
            return null;
        }
        return MongoDBDocumentMapping.mapBsonToDocument(doc);
    }

    @Override
    public Document GetLatestDraft() {
        // No explicit timestamp fields in the model; use MongoDB natural order as a
        // best-effort "latest".
        var filter = new org.bson.Document("status", new org.bson.Document("$in", List.of("BAN_NHAP", "DA_TAI_FILE")));
        var doc = collection.find(filter).sort(new org.bson.Document("$natural", -1)).first();
        if (doc == null) {
            return null;
        }
        return MongoDBDocumentMapping.mapBsonToDocument(doc);
    }

    @Override
    public List<Document> GetAllDocuments() {
        List<org.bson.Document> bsonDocs = collection.find().into(new ArrayList<>());
        return bsonDocs.stream().map(MongoDBDocumentMapping::mapBsonToDocument).collect(Collectors.toList());
    }

    @Override
    public void CreateDocument(Document doc) {
        try {
            var bsonDoc = MongoDBDocumentMapping.mapDocumentToBson(doc);
            collection.insertOne(bsonDoc);
        } catch (MongoException ex) {
            throw new RepositoryException("Thất bại khi tạo hồ sơ với ID: " + doc.id + " trong MongoDB", ex);
        }
    }

    @Override
    public void UpdateDocument(Document doc) {
        try {
            var filter = new org.bson.Document("_id", doc == null ? null : doc.id);
            var bsonDoc = MongoDBDocumentMapping.mapDocumentToBson(doc);
            bsonDoc.remove("_id");
            var update = new org.bson.Document("$set", bsonDoc);
            collection.updateOne(filter, update);
        } catch (MongoException ex) {
            throw new RepositoryException("Thất bại khi cập nhật hồ sơ với ID: " + doc.id + " trong MongoDB", ex);
        }
    }

    @Override
    public void DeleteDocument(UUID id) {
        try {
            var filter = new org.bson.Document("_id", id);
            collection.deleteOne(filter);
        } catch (MongoException ex) {
            throw new RepositoryException("Thất bại khi xóa hồ sơ với ID: " + id + " trong MongoDB", ex);
        }
    }
}
