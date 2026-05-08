package vn.edu.tdtu.edocument.document.repository;

import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.document.repository.cloud.AWS.AWSStorage;
import vn.edu.tdtu.edocument.document.repository.database.MongoDB.MongoDBConfiguration;
import vn.edu.tdtu.edocument.document.repository.database.MongoDB.DocumentRepository;
import vn.edu.tdtu.edocument.document.repository.local_storage.JsonStorage;

public class RepositoryFactory {
    public static IRepository createRepository(RepositoryType type) {
        switch (type) {
            case MONGODB:
                try {
                    // Attempt to create MongoDB repository
                    MongoDBConfiguration mongoConfig = MongoDBConfiguration.getInstance();
                    return new DocumentRepository(mongoConfig);
                } catch (Exception e) {
                    System.err.println("[Factory] MongoDB configuration failed: " + e.getMessage());
                }
                break;
            case AWS:
                try {
                    // Fake AWS repository (stores JSON metadata under server_storage/aws)
                    return AWSStorage.getInstance();
                } catch (Exception e) {
                    System.err.println("[Factory] AWS configuration failed: " + e.getMessage());
                }
                break;
            case JSON:
                return JsonStorage.getInstance();
        }
        throw new IllegalArgumentException("Unsupported repository type: " + type);
    }
}
