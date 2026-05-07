package vn.edu.tdtu.edocument.document.extractor.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;

public class TxtContentExtractor implements FileExtractorStrategy {

    @Override
    public String extractContent(File file) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            return content;
        } catch (IOException e) {
            System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
            return "";
        }
    }

}
