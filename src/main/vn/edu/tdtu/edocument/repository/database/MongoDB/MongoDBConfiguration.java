package vn.edu.tdtu.edocument.repository.database.MongoDB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.UuidRepresentation;

public class MongoDBConfiguration {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private static class Holder {
        private static final MongoDBConfiguration INSTANCE = new MongoDBConfiguration();
    }

    public MongoDBConfiguration() {
        String connectionString = resolve("MONGODB_CONNECTION_STRING");
        String databaseName = resolve("MONGODB_DATABASE_NAME");

        if (connectionString == null || connectionString.isBlank() || databaseName == null || databaseName.isBlank()) {
            throw new IllegalStateException(
                    "MongoDB is not configured. Set MONGODB_CONNECTION_STRING and MONGODB_DATABASE_NAME (env vars, .env, or -D properties).");
        }

        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs)
                // Ensure java.util.UUID is encoded/decoded as BSON UUID (Binary subtype 4).
                .uuidRepresentation(UuidRepresentation.STANDARD).build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(databaseName);
    }

    public static MongoDBConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void close() {
        mongoClient.close();
    }

    private static String resolve(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        return value;
    }
}