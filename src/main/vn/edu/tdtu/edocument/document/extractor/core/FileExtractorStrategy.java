package vn.edu.tdtu.edocument.document.extractor.core;

import java.io.File;

public interface FileExtractorStrategy {
    String extractContent(File file);
}
