package vn.edu.tdtu.edocument.document.extractor.impl;

import java.io.File;

import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;

public class PngContentExtractor implements FileExtractorStrategy {
    private final IOcrService ocrService;

    public PngContentExtractor(IOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public String extractContent(File file) {
        if (!PngChecker.isPng(file)) {
            throw new IllegalArgumentException("Invalid PNG file: " + (file == null ? "null" : file.getName()));
        }
        try {
            return ocrService.extractText(file);
        } catch (OcrException e) {
            throw new RuntimeException("Error extracting text from PNG: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting text from PNG: " + e.getMessage(), e);
        }
    }

    private class PngChecker {
        private static boolean isPng(File file) {
            if (file == null || !file.isFile())
                return false;
            try {
                if (isPngByExtension(file))
                    return true;
                if (isPngByMagic(file))
                    return true;
                return isPngByMime(file);
            } catch (Exception e) {
                return false;
            }
        }

        private static boolean isPngByExtension(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(".png");
        }

        private static boolean isPngByMime(File file) throws java.io.IOException {
            java.nio.file.Path p = file.toPath();
            String mime = java.nio.file.Files.probeContentType(p);
            return mime != null && (mime.equals("image/png") || mime.equals("image/x-png"));
        }

        private static boolean isPngByMagic(File file) {
            byte[] header = new byte[8]; // PNG signature: \211PNG\r\n\032\n
            try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                int read = in.read(header);
                if (read < 8)
                    return false;
                byte[] pngSig = new byte[] { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
                for (int i = 0; i < 8; i++) {
                    if (header[i] != pngSig[i])
                        return false;
                }
                return true;
            } catch (java.io.IOException e) {
                return false;
            }
        }
    }
}
