package vn.edu.tdtu.edocument.ui;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.repository.*;
import vn.edu.tdtu.edocument.repository.database.MongoDB.MongoDBConfiguration;
import vn.edu.tdtu.edocument.repository.database.MongoDB.MongoDBDocumentRepository;
import vn.edu.tdtu.edocument.repository.local_storage.LocalJsonRepository;
import vn.edu.tdtu.edocument.service.DocumentProcessor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MainSwingUI extends JFrame {
    private static final PrintStream ORIGINAL_ERR = System.err;
    static {
        System.err.println("[UI] MainSwingUI class loaded.");
    }
    private JTextArea consoleArea;
    private JTable documentTable;
    private DefaultTableModel tableModel;
    // Configuration and repository setup can be done here or via a factory method.
    // For simplicity, we'll do it directly.
    private IRepository _repository = RepositoryFactory.createRepository(RepositoryType.MONGODB);
    private DocumentProcessor processor = new DocumentProcessor(_repository);
    private List<Document> documentList;

    public MainSwingUI() {
        System.err.println("[UI] Entering MainSwingUI constructor.");

        documentList = new ArrayList<>();

        setTitle("Hệ thống Quản lý Hồ sơ Điện tử - v1.0 (Home)");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = { "Mã hồ sơ", "Người nộp", "Loại hồ sơ", "Trạng thái", "Tập tin" };
        tableModel = new DefaultTableModel(columnNames, 0);
        documentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(documentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách hồ sơ hệ thống"));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(Color.GREEN);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane logScrollPane = new JScrollPane(consoleArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Hệ thống Log/Thông báo"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, logScrollPane);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Thêm mới hồ sơ");
        JButton btnClear = new JButton("Xóa Log");
        toolBar.add(btnAdd);
        toolBar.add(btnClear);
        add(toolBar, BorderLayout.NORTH);

        redirectSystemStreams();
        loadExistingDocumentsAsync();

        btnAdd.addActionListener(e -> {
            AddDocumentWizardDialog dialog = new AddDocumentWizardDialog(this, processor, _repository);
            dialog.setVisible(true);
            refreshTable();
        });

        btnClear.addActionListener(e -> consoleArea.setText(""));

        // Click on a row to load data from documentList and print details to console.
        documentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 1) {
                    return;
                }

                int viewRow = documentTable.rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }

                documentTable.setRowSelectionInterval(viewRow, viewRow);
                printSelectedDocumentToConsole();
            }
        });
    }

    private void printSelectedDocumentToConsole() {
        int viewRow = documentTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int modelRow = documentTable.convertRowIndexToModel(viewRow);
        Object rawId = tableModel.getValueAt(modelRow, 0);
        String docId = rawId == null ? null : rawId.toString();
        if (docId == null || docId.isBlank()) {
            System.out.println("[UI] Không tìm thấy mã hồ sơ ở dòng được chọn.");
            return;
        }

        Document matched = null;
        for (Document doc : documentList) {
            if (doc != null && docId.equals(doc.id.toString())) {
                matched = doc;
                break;
            }
        }

        if (matched == null) {
            System.out.println("[UI] Không tìm thấy hồ sơ trong documentList với mã: " + docId);
            return;
        }

        System.out.println("\n--- CHI TIẾT HỒ SƠ: " + matched.id + " ---");
        System.out.println("Người nộp: " + matched.applicantName + " | Email: " + matched.applicantEmail + " | SĐT: "
                + matched.applicantPhone);
        System.out.println("Cán bộ tiếp nhận: " + matched.officerName + " | Email: " + matched.officerEmail + " | SĐT: "
                + matched.officerPhone);
        System.out.println("Loại hồ sơ: " + matched.documentType);
        System.out.println("Đường dẫn tệp: " + matched.filePath + " (" + matched.fileSizeKB + " KB)");
        System.out.println("Chữ ký số: " + matched.digitalSignature);
        System.out.println("Trạng thái hiện tại: " + matched.status);
        System.out.println("----------------------------------------\n");
    }

    private void loadExistingDocumentsAsync() {
        // Loading/parsing can be heavy (large extractedContent), so do it off the EDT.
        SwingWorker<List<Document>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Document> doInBackground() {
                try {
                    /// đổi lại _repository khác nếu muốn load từ một nguồn khác (ví dụ: database,
                    /// API, v.v.)
                    return _repository.GetAllDocuments();
                } catch (Exception e) {
                    ORIGINAL_ERR.println("[UI] Failed to load existing documents: " + e.getMessage());
                    e.printStackTrace(ORIGINAL_ERR);
                    return List.of();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Document> docs = get();
                    documentList.clear();
                    documentList.addAll(docs);
                    refreshTable();
                    ORIGINAL_ERR.println("[UI] Loaded " + docs.size() + " document(s).");
                } catch (Exception e) {
                    ORIGINAL_ERR.println("[UI] Failed to finalize load: " + e.getMessage());
                    e.printStackTrace(ORIGINAL_ERR);
                }
            }
        };
        worker.execute();
    }

    public void addDocumentToList(Document doc) {
        documentList.add(doc);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Document doc : documentList) {
            tableModel.addRow(
                    new Object[] { doc.id, doc.applicantName, doc.documentType, doc.status, doc.fileExtension });
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            ORIGINAL_ERR.println("[UI] LookAndFeel error: " + e.getMessage());
            e.printStackTrace(ORIGINAL_ERR);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                MainSwingUI ui = new MainSwingUI();
                ui.setVisible(true);
                ORIGINAL_ERR.println("[UI] MainSwingUI launched.");
            } catch (Exception e) {
                ORIGINAL_ERR.println("[UI] Failed to initialize MainSwingUI: " + e.getMessage());
                e.printStackTrace(ORIGINAL_ERR);
            }
        });
    }

}