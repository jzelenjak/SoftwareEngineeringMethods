package nl.tudelft.sem.hiring.procedure.contracts;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Contract {
    private final float fontSize = 20;
    private final Position taPos = new Position(152, 642);
    private final Position courseCodePos = new Position(175, 613);
    private final Position startDatePos = new Position(150, 510);
    private final Position endDatePos = new Position(390, 510);
    private final Position maxHoursPos = new Position(250, 483);

    private String taName;
    private String courseCode;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private int maxHours = 200;

    /**
     * Full args constructor.
     *
     * @param taName the name of the TA
     * @param courseCode the code of the course
     * @param startDate the start date of the course in ZonedDateTime format
     * @param endDate the end date of the course in ZonedDateTime format
     * @param maxHours the maximum hours the TA is allowed to declare
     */
    public Contract(String taName, String courseCode,
                    ZonedDateTime startDate, ZonedDateTime endDate, int maxHours) {
        this.taName = taName;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxHours = maxHours;
    }

    /**
     * Zero args constructor.
     */
    public Contract() {
    }

    /**
     * Method for generating a byte array corresponding to the personalized contract.
     *
     * @return byte array of the pdf
     * @throws IOException if the resource is not present on the server or other issues
     * @throws DocumentException if PdfStamper does not work
     */
    public byte[] generate() throws IOException, DocumentException {
        URL baseContract;

        baseContract = Thread.currentThread().getContextClassLoader()
                .getResource("templatecontract.pdf");

        if (baseContract == null) {
            throw new IOException("Resource not found");
        }

        BaseFont bf = BaseFont.createFont(
                BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(baseContract); // input PDF
        PdfStamper stamper = new PdfStamper(reader, outputStream); // output PDF

        if (this.taName != null) {
            addText(taName, taPos, stamper, bf);
        }
        if (this.courseCode != null) {
            addText(courseCode, courseCodePos, stamper, bf);
        }
        if (this.startDate != null) {
            addText(startDate.toLocalDate().toString(), startDatePos, stamper, bf);
        }
        if (this.endDate != null) {
            addText(endDate.toLocalDate().toString(), endDatePos, stamper, bf);
        }
        addText(String.valueOf(maxHours), maxHoursPos, stamper, bf);

        stamper.close();
        return outputStream.toByteArray();
    }

    private void addText(String text, Position pos, PdfStamper stamper, BaseFont bf) {
        PdfContentByte over = stamper.getOverContent(1);

        over.beginText();
        over.setFontAndSize(bf, fontSize);    // set font and size
        over.setTextMatrix(pos.getPosX(), pos.getPosY());   // 0,0 is at the bottom left
        over.showText(text);  // set text
        over.endText();
    }
}
