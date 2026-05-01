package vn.edu.tdtu.edocument.document.extractor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;

/**
 * Trích xuất nội dung từ file PDF. <br>
 * Hỗ trợ pdf có text layer
 */
public class PdfContentExtractor implements FileExtractorStrategy {
    @Override
    public String extractContent(File file) {
        if (!isPdf(file)) {
            throw new IllegalArgumentException("Invalid PDF file: " + file.getName());
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            String extractedContent = "";
            PDFTextStripper stripper = new PDFTextStripper();
            extractedContent = stripper.getText(document);
            return extractedContent;
        } catch (IOException e) {
            throw new RuntimeException("Error extracting PDF content: " + e.getMessage(), e);
        }
    }

    private static boolean isPdf(File file) {
        if (file == null || !file.isFile())
            return false;
        try {
            if (isPdfByExtension(file))
                return true;
            if (isPdfByMagic(file))
                return true;
            return isPdfByMime(file);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isPdfByExtension(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
    }

    private static boolean isPdfByMime(File file) throws IOException {
        Path p = file.toPath();
        String mime = Files.probeContentType(p);
        return mime != null && mime.equals("application/pdf");
    }

    private static boolean isPdfByMagic(File file) {
        byte[] pdfHeader = new byte[5]; // "%PDF-"
        try (FileInputStream in = new FileInputStream(file)) {
            int read = in.read(pdfHeader);
            if (read < 5)
                return false;
            String header = new String(pdfHeader, StandardCharsets.ISO_8859_1);
            return header.equals("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }

}
