package nl.tudelft.sem.hour.management.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

public class HourDeclarationTest {

    @Test
    public void testConstructor() {
        HourDeclaration declaration = new HourDeclaration(1234, 5678, 10.5);
        assertThat(declaration).isNotEqualTo(null);
    }

    @Test
    public void testEqualsSameObject() {
        HourDeclaration declaration = new HourDeclaration(1234, 5678, 10.5);
        assertThat(declaration).isEqualTo(declaration);
    }

    @Test
    public void testEquals() {
        HourDeclaration declaration1 = new HourDeclaration(1234, 5678, 10.5);
        HourDeclaration declaration2 = new HourDeclaration(1234, 5678, 10.5);
        assertThat(declaration1).isEqualTo(declaration2);
        assertNotSame(declaration1, declaration2);
    }

    @Test
    public void testEqualsNull() {
        HourDeclaration declaration1 = new HourDeclaration(1234, 5678, 10.5);
        assertThat(declaration1).isNotEqualTo(null);
    }

    @Test
    public void testEqualsObject() {
        HourDeclaration declaration1 = new HourDeclaration(1234, 5678, 10.5);
        assertThat(declaration1).isNotEqualTo(new Object());
    }

    @Test
    public void testEqualsNotEqual() {
        HourDeclaration declaration1 = new HourDeclaration(1, 1234, 5678, 10.5);
        HourDeclaration declaration2 = new HourDeclaration(2, 1234, 5678, 10.5);
        HourDeclaration declaration3 = new HourDeclaration(1, 4321, 5678, 10.5);
        HourDeclaration declaration4 = new HourDeclaration(1, 1234, 8765, 10.5);
        HourDeclaration declaration5 = new HourDeclaration(1, 1234, 5678, 15.0);
        assertThat(declaration1).isNotEqualTo(declaration2);
        assertThat(declaration1).isNotEqualTo(declaration3);
        assertThat(declaration1).isNotEqualTo(declaration4);
        assertThat(declaration1).isNotEqualTo(declaration5);
    }

    @Test
    public void testHashcode() {
        HourDeclaration declaration1 = new HourDeclaration(1, 1234, 5678, 10.5);
        HourDeclaration declaration2 = new HourDeclaration(2, 1234, 5678, 10.5);
        assertThat(declaration1.hashCode()).isEqualTo(declaration1.hashCode());
        assertThat(declaration1.hashCode()).isNotEqualTo(declaration2.hashCode());
    }

}
