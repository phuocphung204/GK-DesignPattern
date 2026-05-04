package vn.edu.tdtu.edocument.RepositoryPattern.database.MongoDB;

public class MongoDBConfiguration {
    private String host;
    private int port;
    private String database;

    public MongoDBConfiguration(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public void connect() {
        System.out.println("Kết nối đến MongoDB...");
    }
}
