package vn.edu.tdtu.edocument.document.extractor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;

/**
 * Trích xuất nội dung từ file PDF. <br>
 * Hỗ trợ pdf có text layer
 */
public class PdfContentExtractor implements FileExtractorStrategy {

    private final IOcrService ocrService;

    public PdfContentExtractor(IOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public String extractContent(File file) {
        if (!PdfChecker.isPdf(file)) {
            throw new IllegalArgumentException("Invalid PDF file: " + file.getName());
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            // Only run when the whole document is either text-only or image-only
            boolean isTextOnly = PdfChecker.isDocumentTextOnly(document);
            boolean isImageOnly = PdfChecker.isDocumentImageOnly(document);

            if (isTextOnly) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }

            if (isImageOnly) {
                var textBuilder = new StringBuilder();
                var imageFiles = extractImagesFromDocument(document);
                for (File img : imageFiles) {
                    String text = ocrService.extractText(img);
                    textBuilder.append(text).append("\n");
                }
                return textBuilder.toString();
            }

            throw new IllegalStateException("Unexpected state: document is neither text-only nor image-only");
        } catch (OcrException e) {
            throw new RuntimeException("Error during OCR processing: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error extracting PDF content: " + e.getMessage(), e);
        }
    }

    /**
     * Trích xuất tất cả hình ảnh từ PDF và lưu tạm vào file, trả về list file ảnh.
     * Các file này sẽ được xóa khi JVM kết thúc.
     * 
     * @param document
     * @return list file ảnh tạm
     * @throws IOException
     */
    private List<File> extractImagesFromDocument(PDDocument document) throws IOException {
        List<File> imageFiles = new ArrayList<>();
        for (PDPage page : document.getPages()) {
            var resources = page.getResources();
            if (resources == null)
                continue;
            Iterable<COSName> names = resources.getXObjectNames();
            for (COSName name : names) {
                var xobj = resources.getXObject(name);
                if (xobj instanceof PDImageXObject) {
                    PDImageXObject img = (PDImageXObject) xobj;
                    File tempFile = File.createTempFile("pdf_image_", ".png");
                    tempFile.deleteOnExit();
                    ImageIO.write(img.getImage(), "png", tempFile);
                    imageFiles.add(tempFile);
                }
            }
        }
        return imageFiles;
    }

    private class PdfChecker {

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

        public static boolean pageContainsOnlyImages(PDPage page, PDDocument doc) throws IOException {
            // Simple heuristic: try to extract text from this single page
            PDFTextStripper stripper = new PDFTextStripper();
            int pageIndex = doc.getPages().indexOf(page) + 1;
            stripper.setStartPage(pageIndex);
            stripper.setEndPage(pageIndex);
            String text = stripper.getText(doc).trim();
            if (!text.isEmpty())
                return false;
            // If no text, check resources for image XObjects
            var resources = page.getResources();
            if (resources == null)
                return true;
            Iterable<COSName> names = resources.getXObjectNames();
            boolean hasImage = false;
            for (COSName name : names) {
                var xobj = resources.getXObject(name);
                if (xobj instanceof PDImageXObject) {
                    hasImage = true;
                } else {
                    // could be form XObject which may contain text; treat as unknown
                    return false;
                }
            }
            return hasImage;
        }

        public static boolean pageContainsText(PDPage page, PDDocument doc) throws IOException {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageIndex = doc.getPages().indexOf(page) + 1;
            stripper.setStartPage(pageIndex);
            stripper.setEndPage(pageIndex);
            String text = stripper.getText(doc).trim();
            return !text.isEmpty();
        }

        public static boolean isDocumentTextOnly(PDDocument doc) throws IOException {
            for (PDPage page : doc.getPages()) {
                if (!pageContainsText(page, doc)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean isDocumentImageOnly(PDDocument doc) throws IOException {
            for (PDPage page : doc.getPages()) {
                if (!pageContainsOnlyImages(page, doc)) {
                    return false;
                }
            }
            return true;
        }
    }
}
