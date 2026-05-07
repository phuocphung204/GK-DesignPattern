package vn.edu.tdtu.edocument.document.extractor.impl;

import java.io.File;

import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;

public class JpgContentExtractor implements FileExtractorStrategy {
    private final IOcrService ocrService;

    public JpgContentExtractor(IOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public String extractContent(File file) {
        if (!JpegChecker.isJpeg(file)) {
            throw new IllegalArgumentException("Invalid JPEG file: " + (file == null ? "null" : file.getName()));
        }

        try {
            return ocrService.extractText(file);
        } catch (OcrException e) {
            throw new RuntimeException("Error extracting text from JPEG: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting text from JPEG: " + e.getMessage(), e);
        }
    }

    private static class JpegChecker {

        private static boolean isJpeg(File file) {
            if (file == null || !file.isFile())
                return false;
            try {
                if (isJpegByExtension(file))
                    return true;
                if (isJpegByMagic(file))
                    return true;
                return isJpegByMime(file);
            } catch (Exception e) {
                return false;
            }
        }

        private static boolean isJpegByExtension(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".jpeg");
        }

        private static boolean isJpegByMime(File file) throws java.io.IOException {
            java.nio.file.Path p = file.toPath();
            String mime = java.nio.file.Files.probeContentType(p);
            return mime != null && mime.equals("image/jpeg");
        }

        private static boolean isJpegByMagic(File file) {
            byte[] header = new byte[3]; // JPEG starts with 0xFF 0xD8 0xFF
            try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                int read = in.read(header);
                if (read < 3)
                    return false;
                return (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF);
            } catch (java.io.IOException e) {
                return false;
            }
        }
    }
}
