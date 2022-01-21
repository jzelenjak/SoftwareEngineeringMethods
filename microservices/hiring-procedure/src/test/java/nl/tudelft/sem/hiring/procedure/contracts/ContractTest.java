package nl.tudelft.sem.hiring.procedure.contracts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class ContractTest {

    private static final String TA_NAME = "Mihnea";
    private static final String COURSE_CODE = "CSE1215";

    @Test
    public void constructorTest() {
        Contract contract = new Contract();
        assertNotNull(contract);
    }

    @Test
    public void fullArgsConstructorTest() {
        Contract contract = new Contract(TA_NAME, COURSE_CODE,
                ZonedDateTime.now(), ZonedDateTime.now(), 100);
        assertNotNull(contract);
        assertNotNull(contract.getCourseCode());
        assertNotNull(contract.getTaName());
        assertNotNull(contract.getStartDate());
        assertNotNull(contract.getEndDate());
    }

    @Test
    public void generatePdfTestPass() throws DocumentException, IOException {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime tomorrow = now.plusDays(1);
        Contract contract = new Contract(TA_NAME, COURSE_CODE,
                now, tomorrow, 100);
        assertNotNull(contract);
        assertDoesNotThrow(contract::generate);
        byte[] generatedContract = contract.generate();
        PdfReader reader = new PdfReader(generatedContract);
        String content = PdfTextExtractor.getTextFromPage(reader, 1);
        assertTrue(content.contains(TA_NAME));
        assertTrue(content.contains(COURSE_CODE));
        assertTrue(content.contains(now.toLocalDate().toString()));
        assertTrue(content.contains(tomorrow.toLocalDate().toString()));
        assertTrue(content.contains("100"));
    }

    @Test
    public void generatePdfNoName() throws DocumentException, IOException {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime tomorrow = now.plusDays(1);
        Contract contract = new Contract();
        assertNotNull(contract);
        contract.setCourseCode(COURSE_CODE);
        contract.setStartDate(now);
        contract.setEndDate(tomorrow);
        contract.setMaxHours(100);
        byte[] generatedContract = contract.generate();
        PdfReader reader = new PdfReader(generatedContract);
        String content = PdfTextExtractor.getTextFromPage(reader, 1);
        assertFalse(content.contains(TA_NAME));
    }
}
