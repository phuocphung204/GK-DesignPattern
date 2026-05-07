package vn.edu.tdtu.edocument.RepositoryPattern.database.SqlServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SqlServerConfiguration {
    private static final String CONNECTION_URL =
                "jdbc:sqlserver://DESKTOP-67TD236\\SQLEXPRESS;"
                + "IntegratedSecurity=true;"
                + "Encrypt=false;"
                + "TrustServerCertificate=true;"
                + "LoginTimeout=30;";

    private static final SqlServerConfiguration _instance = new SqlServerConfiguration(); // Singleton instance EAGER initialization

    private SqlServerConfiguration() {
        // Private constructor to prevent instantiation
    }

    public static SqlServerConfiguration getInstance() {
        return _instance;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    public void disconnect(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void executeQuery(String query) {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addDocumentData(String jsonData) {
        String query = "INSERT INTO Documents (JsonData) VALUES (?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, jsonData);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateDocumentStatus(String documentId, String newStatus) {
        String query = "UPDATE Documents SET Status = ? WHERE Id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newStatus);
            statement.setString(2, documentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteDocument(String documentId) {
        String query = "DELETE FROM Documents WHERE Id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getDocument(String documentId) {
        String query = "SELECT Id, ApplicantName, ApplicantEmail, ApplicantPhone, "
                + "OfficerName, OfficerEmail, OfficerPhone, DocumentType, "
                + "FilePath, FileExtension, FileSizeKB, DigitalSignature, "
                + "ExtractedContent, Status FROM Documents WHERE Id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    StringBuilder json = new StringBuilder();
                    json.append("{");
                    json.append("\"id\":\"").append(escapeJson(resultSet.getString("Id"))).append("\",");
                    json.append("\"applicantName\":\"").append(escapeJson(resultSet.getString("ApplicantName"))).append("\",");
                    json.append("\"applicantEmail\":\"").append(escapeJson(resultSet.getString("ApplicantEmail"))).append("\",");
                    json.append("\"applicantPhone\":\"").append(escapeJson(resultSet.getString("ApplicantPhone"))).append("\",");
                    json.append("\"officerName\":\"").append(escapeJson(resultSet.getString("OfficerName"))).append("\",");
                    json.append("\"officerEmail\":\"").append(escapeJson(resultSet.getString("OfficerEmail"))).append("\",");
                    json.append("\"officerPhone\":\"").append(escapeJson(resultSet.getString("OfficerPhone"))).append("\",");
                    json.append("\"documentType\":\"").append(escapeJson(resultSet.getString("DocumentType"))).append("\",");
                    json.append("\"filePath\":\"").append(escapeJson(resultSet.getString("FilePath"))).append("\",");
                    json.append("\"fileExtension\":\"").append(escapeJson(resultSet.getString("FileExtension"))).append("\",");
                    json.append("\"fileSizeKB\":").append(resultSet.getLong("FileSizeKB")).append(",");
                    json.append("\"digitalSignature\":\"").append(escapeJson(resultSet.getString("DigitalSignature"))).append("\",");
                    json.append("\"extractedContent\":\"").append(escapeJson(resultSet.getString("ExtractedContent"))).append("\",");
                    json.append("\"status\":\"").append(escapeJson(resultSet.getString("Status"))).append("\"");
                    json.append("}");
                    return json.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
