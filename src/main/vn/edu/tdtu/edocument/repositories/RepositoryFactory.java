package vn.edu.tdtu.edocument.repositories;

import vn.edu.tdtu.edocument.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.repositories.database.MongoDB.MongoDBConfiguration;
import vn.edu.tdtu.edocument.repositories.database.MongoDB.DocumentRepository;
import vn.edu.tdtu.edocument.repositories.local_storage.JsonStorage;

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
                    // Implement AWS repository creation if needed
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
