package nl.tudelft.sem.hour.management.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.time.ZonedDateTime;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import org.junit.jupiter.api.Test;

public class HourDeclarationTest {

    private final transient ZonedDateTime dateTime = ZonedDateTime.now();
    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(1234, 5678, 10.5, "de");
    private final transient HourDeclarationRequest hourDeclarationRequest1 =
            new HourDeclarationRequest(4321, 5678, 10.5, "nl");
    private final transient HourDeclarationRequest hourDeclarationRequest2 =
            new HourDeclarationRequest(1234, 8765, 10.5, "tr");

    @Test
    public void testConstructor() {
        HourDeclaration declaration = new HourDeclaration(hourDeclarationRequest);
        assertThat(declaration).isNotEqualTo(null);
    }

    @Test
    public void testEqualsSameObject() {
        HourDeclaration declaration = new HourDeclaration(hourDeclarationRequest);
        assertThat(declaration).isEqualTo(declaration);
    }

    @Test
    public void testEquals() {
        HourDeclaration declaration1 = new HourDeclaration(hourDeclarationRequest);
        HourDeclaration declaration2 = new HourDeclaration(hourDeclarationRequest);
        assertThat(declaration1).isEqualTo(declaration2);
        assertNotSame(declaration1, declaration2);
    }

    @Test
    public void testEqualsNull() {
        HourDeclaration declaration1 = new HourDeclaration(hourDeclarationRequest);
        assertNotNull(declaration1);
    }

    @Test
    public void testEqualsObject() {
        HourDeclaration declaration1 = new HourDeclaration(hourDeclarationRequest);
        assertThat(declaration1).isNotEqualTo(new Object());
    }

    @Test
    public void testEqualsNotEqual() {
        HourDeclaration declaration1 = new HourDeclaration(1, hourDeclarationRequest1,
                false, dateTime);
        HourDeclaration declaration2 = new HourDeclaration(2, hourDeclarationRequest2,
                false, dateTime);
        assertThat(declaration1).isNotEqualTo(declaration2);
    }

    @Test
    public void testHashcode() {
        HourDeclaration declaration1 = new HourDeclaration(1,
                hourDeclarationRequest, false, dateTime);
        HourDeclaration declaration2 = new HourDeclaration(2,
                hourDeclarationRequest, false, dateTime);
        assertThat(declaration1.hashCode()).isEqualTo(declaration1.hashCode());
        assertThat(declaration1.hashCode()).isNotEqualTo(declaration2.hashCode());
    }

}
