package vn.edu.tdtu.edocument.RepositoryPattern.database.MongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConfiguration {
    private static final String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private static final MongoDBConfiguration instance = new MongoDBConfiguration(); // Singleton instance (eager)

    public MongoDBConfiguration() {
        mongoClient = MongoClients.create(CONNECTION_STRING);
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoDBConfiguration getInstance() {
        return instance;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void close() {
        mongoClient.close();
    }
}