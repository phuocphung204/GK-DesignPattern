package vn.edu.tdtu.edocument.ui;

import vn.edu.tdtu.edocument.document.builder.DocumentBuilder;
import vn.edu.tdtu.edocument.document.builder.IDocumentBuilder;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.service.DocumentProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.util.EnumMap;
import java.util.Locale;

public class AddDocumentWizardDialog extends JDialog {
    private static final String STEP_PERSONAL = "PERSONAL";
    private static final String STEP_FILE = "FILE";
    private static final String STEP_CONFIRM = "CONFIRM";

    private static final int FORM_PADDING = 16;
    private static final int V_GAP = 8;

    private final DocumentProcessor processor;
    private final MainSwingUI parent;
    private final IRepository _repository;
    private final IDocumentBuilder docBuilder;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private JTextField txtApplicantName;
    private JTextField txtApplicantEmail;
    private JTextField txtApplicantPhone;

    private final EnumMap<NotificationChannelType, JCheckBox> applicantNotificationChannels = new EnumMap<>(
            NotificationChannelType.class);

    private JComboBox<DocumentTypes> cbDocumentType;
    private JTextField txtDigitalSignature;
    private JLabel lblFileName;
    private File selectedFile;

    private JTextField txtOfficerName;
    private JTextField txtOfficerEmail;
    private JTextField txtOfficerPhone;

    private final EnumMap<NotificationChannelType, JCheckBox> officerNotificationChannels = new EnumMap<>(
            NotificationChannelType.class);

    private JButton btnBack;
    private JButton btnNext;
    private JButton btnFinish;

    private int stepIndex = 0;

    public AddDocumentWizardDialog(MainSwingUI parent, DocumentProcessor processor, IRepository repository) {
        super(parent, "Tao ho so moi", true);
        this.parent = parent;
        this.processor = processor;
        this._repository = repository;
        Document latestDraft = fetchLatestDraft();
        this.docBuilder = (latestDraft != null) ? new DocumentBuilder(latestDraft) : new DocumentBuilder();

        setLayout(new BorderLayout());

        cardPanel.add(buildPersonalPanel(), STEP_PERSONAL);
        cardPanel.add(buildFilePanel(), STEP_FILE);
        cardPanel.add(buildConfirmPanel(), STEP_CONFIRM);
        add(cardPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
        if (latestDraft != null) {
            populateFromDraft(latestDraft);
        }
        updateButtonState();

        pack();
        if (getWidth() < 520) {
            setSize(new Dimension(520, getHeight()));
        }
        setLocationRelativeTo(parent);
    }

    private JPanel buildPersonalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

        JLabel lblApplicantName = new JLabel("Ten nguoi nop:");
        lblApplicantName.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblApplicantName);
        txtApplicantName = new JTextField();
        txtApplicantName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtApplicantName.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtApplicantName.getPreferredSize().height));
        formPanel.add(txtApplicantName);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblApplicantEmail = new JLabel("Email nguoi nop:");
        lblApplicantEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblApplicantEmail);
        txtApplicantEmail = new JTextField();
        txtApplicantEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtApplicantEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtApplicantEmail.getPreferredSize().height));
        formPanel.add(txtApplicantEmail);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblApplicantPhone = new JLabel("SDT nguoi nop:");
        lblApplicantPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblApplicantPhone);
        txtApplicantPhone = new JTextField();
        txtApplicantPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtApplicantPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtApplicantPhone.getPreferredSize().height));
        formPanel.add(txtApplicantPhone);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        // List chọn gửi thông báo cho người nộp đơn (email, SMS, AppPush)
        JLabel lblApplicantNotify = new JLabel("Thong bao nguoi nop:");
        lblApplicantNotify.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblApplicantNotify);

        formPanel.add(buildNotificationChannelPanel(applicantNotificationChannels));

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

        JLabel lblDocumentType = new JLabel("Loai ho so:");
        lblDocumentType.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDocumentType);

        cbDocumentType = new JComboBox<>(DocumentTypes.values());
        cbDocumentType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDocumentType.setMaximumSize(new Dimension(Integer.MAX_VALUE, cbDocumentType.getPreferredSize().height));
        formPanel.add(cbDocumentType);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblSignature = new JLabel("Chu ky so:");
        lblSignature.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblSignature);

        txtDigitalSignature = new JTextField();
        txtDigitalSignature.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDigitalSignature
                .setMaximumSize(new Dimension(Integer.MAX_VALUE, txtDigitalSignature.getPreferredSize().height));
        formPanel.add(txtDigitalSignature);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblAttachment = new JLabel("Tap tin dinh kem:");
        lblAttachment.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblAttachment);

        JButton btnFile = new JButton("Chon...");
        lblFileName = new JLabel("Chua chon");
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.add(btnFile);
        filePanel.add(Box.createHorizontalStrut(8));
        filePanel.add(lblFileName);
        formPanel.add(filePanel);

        btnFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                lblFileName.setText(selectedFile.getName());
            }
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildConfirmPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

        JLabel lblOfficerName = new JLabel("Ten can bo:");
        lblOfficerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblOfficerName);
        txtOfficerName = new JTextField("Can bo truc ban");
        txtOfficerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtOfficerName.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtOfficerName.getPreferredSize().height));
        formPanel.add(txtOfficerName);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblOfficerEmail = new JLabel("Email can bo:");
        lblOfficerEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblOfficerEmail);
        txtOfficerEmail = new JTextField("officer@tdtu.edu.vn");
        txtOfficerEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtOfficerEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtOfficerEmail.getPreferredSize().height));
        formPanel.add(txtOfficerEmail);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        JLabel lblOfficerPhone = new JLabel("SDT can bo:");
        lblOfficerPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblOfficerPhone);
        txtOfficerPhone = new JTextField("0123456789");
        txtOfficerPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtOfficerPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtOfficerPhone.getPreferredSize().height));
        formPanel.add(txtOfficerPhone);
        formPanel.add(Box.createVerticalStrut(V_GAP));

        // List chọn gửi thông báo cho cán bộ xử lý (email, SMS, AppPush)
        JLabel lblOfficerNotify = new JLabel("Thong bao can bo:");
        lblOfficerNotify.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblOfficerNotify);

        formPanel.add(buildNotificationChannelPanel(officerNotificationChannels));

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotificationChannelPanel(EnumMap<NotificationChannelType, JCheckBox> target) {
        target.clear();
        JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        channelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (NotificationChannelType type : NotificationChannelType.values()) {
            JCheckBox checkBox = new JCheckBox(type.getDisplayName(), NotificationChannelType.defaultPreference().contains(type));
            target.put(type, checkBox);
            channelPanel.add(checkBox);
        }

        return channelPanel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBack = new JButton("Truoc");
        btnNext = new JButton("Tiep theo");
        btnFinish = new JButton("Hoan thanh");
        JButton btnCancel = new JButton("Huy bo");

        btnBack.addActionListener(e -> moveStep(-1));
        btnNext.addActionListener(e -> moveStep(1));
        btnFinish.addActionListener(e -> finishWizard());
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnBack);
        panel.add(btnNext);
        panel.add(btnFinish);
        panel.add(btnCancel);
        return panel;
    }

    private void moveStep(int delta) {
        if (delta > 0 && !commitCurrentStep()) {
            return;
        }
        stepIndex += delta;
        if (stepIndex < 0) {
            stepIndex = 0;
        } else if (stepIndex > 2) {
            stepIndex = 2;
        }
        showStep();
    }

    private void showStep() {
        switch (stepIndex) {
        case 0:
            cardLayout.show(cardPanel, STEP_PERSONAL);
            break;
        case 1:
            cardLayout.show(cardPanel, STEP_FILE);
            break;
        case 2:
            cardLayout.show(cardPanel, STEP_CONFIRM);
            break;
        default:
            cardLayout.show(cardPanel, STEP_PERSONAL);
        }
        updateButtonState();
    }

    private void updateButtonState() {
        btnBack.setEnabled(stepIndex > 0);
        btnNext.setEnabled(stepIndex < 2);
        btnFinish.setEnabled(stepIndex == 2);
    }

    private Document fetchLatestDraft() {
        // Lấy bản nháp gần nhất từ repository, nếu có
        return _repository.GetLatestDraft();
    }

    private void populateFromDraft(Document draft) {
        if (draft.applicantName != null) {
            txtApplicantName.setText(draft.applicantName);
        }
        if (draft.applicantEmail != null) {
            txtApplicantEmail.setText(draft.applicantEmail);
        }
        if (draft.applicantPhone != null) {
            txtApplicantPhone.setText(draft.applicantPhone);
        }
        if (draft.documentType != null) {
            cbDocumentType.setSelectedItem(draft.documentType);
        }
        if (draft.digitalSignature != null) {
            txtDigitalSignature.setText(draft.digitalSignature);
        }
        if (draft.filePath != null && !draft.filePath.isBlank()) {
            File draftFile = new File(draft.filePath);
            if (draftFile.exists()) {
                selectedFile = draftFile;
                lblFileName.setText(draftFile.getName());
            }
        }
        if (draft.officerName != null) {
            txtOfficerName.setText(draft.officerName);
        }
        if (draft.officerEmail != null) {
            txtOfficerEmail.setText(draft.officerEmail);
        }
        if (draft.officerPhone != null) {
            txtOfficerPhone.setText(draft.officerPhone);
        }

        if (draft.status == DocumentStatus.DA_TAI_FILE) {
            stepIndex = 2;
        } else {
            stepIndex = 0;
        }
        showStep();
    }

    private boolean commitCurrentStep() {
        if (stepIndex == 0) {

            String name = txtApplicantName.getText().trim();
            String email = txtApplicantEmail.getText().trim();
            String phone = txtApplicantPhone.getText().trim();
            List<NotificationChannelType> applicantPreference = applicantNotificationChannels.entrySet().stream()
                    .filter(e -> e.getValue().isSelected()).map(e -> e.getKey()).toList();

            // Tạo builder tạm thời để kiểm tra thông tin cá nhân, không cần lưu vào builder
            // chính thức nếu chưa qua bước xác thực file
            docBuilder.SetPersonalInfo(name, email, phone, applicantPreference);
            // Kiểm tra thông tin cá nhân đã nhập, nếu thiếu sẽ không cho phép qua bước tiếp
            // theo
            boolean isValid = processor.proccessInsertPersonalInfo(docBuilder.Build());

            if (!isValid) {
                JOptionPane.showMessageDialog(this, "Vui long nhap day du thong tin nguoi nop.", "Loi",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }

        if (stepIndex == 1) {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Vui long chon tap tin dinh kem.", "Loi",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String ext = extractExtension(selectedFile.getName());
            if (ext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khong tim thay duoi file.", "Loi", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String extension = ext.toUpperCase(Locale.ROOT);

            long sizeKb = selectedFile.length() / 1024;
            // tạo builder tạm thời để kiểm tra thông tin file, không cần lưu vào builder
            // chính thức nếu chưa qua bước xác thực
            docBuilder.SetFileInfo((DocumentTypes) cbDocumentType.getSelectedItem(), selectedFile.getAbsolutePath(),
                    extension, sizeKb, txtDigitalSignature.getText().trim());
            // Kiểm tra thông tin file đã nhập, nếu thiếu hoặc không hợp lệ sẽ không cho
            // phép qua bước tiếp theo
            boolean isValid = processor.proccessInsertDocumentFile(docBuilder.Build());
            if (!isValid) {
                JOptionPane.showMessageDialog(this, "File tai lieu khong hop le. Vui long kiem tra log.", "Loi",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
        return true;
    }

    private void finishWizard() {
        String officerName = txtOfficerName.getText().trim();
        String officerEmail = txtOfficerEmail.getText().trim();
        String officerPhone = txtOfficerPhone.getText().trim();
        List<NotificationChannelType> officerPreference = officerNotificationChannels.entrySet().stream()
            .filter(e -> e.getValue().isSelected()).map(e -> e.getKey()).toList();
        // Tạo builder hoàn chỉnh với tất cả thông tin đã nhập, bao gồm cả thông tin cán
        // bộ xử lý
        docBuilder.SetSubmissionInfo(officerName, officerEmail, officerPhone, officerPreference);
        Document doc = docBuilder.Build();
        // Xử lý thông tin nộp hồ sơ, nếu có lỗi sẽ không hoàn thành wizard và sẽ hiển
        // thị lỗi
        boolean isValid = processor.proccessInsertSubmissionInfo(doc);
        if (!isValid) {
            JOptionPane.showMessageDialog(this, "Thong tin can bo xu ly khong hop le. Vui long kiem tra log.", "Loi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DocumentStatus.DA_TIEP_NHAN.equals(doc.status)) {
            parent.refreshTable();
            parent.addDocumentToList(doc);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ho so khong hop le. Vui long kiem tra log.", "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }
}
