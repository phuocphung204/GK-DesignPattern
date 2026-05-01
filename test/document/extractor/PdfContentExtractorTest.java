package test.document.extractor;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import vn.edu.tdtu.edocument.document.extractor.impl.PdfContentExtractor;
import java.io.File;

public class PdfContentExtractorTest {

    @Test
    public void extractTextFromSimplePdf() {
        PdfContentExtractor extractor = new PdfContentExtractor();
        File pdf = new File("server_storage/sample-text.pdf"); // chuẩn bị sample
        assertTrue("tệp PDF mẫu phải tồn tại", pdf.exists());
        String text = extractor.extractContent(pdf);
        assertThat(text, is(notNullValue()));
        assertTrue("văn bản trích xuất không được rỗng", text.length() > 0);
        assertThat("văn bản trích xuất phải chứa nội dung mẫu", text, containsString("Đây là pdf có text layer"));
    }
}