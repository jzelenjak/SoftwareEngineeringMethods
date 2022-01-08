package nl.tudelft.sem.hiring.procedure.contracts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class ContractTest {

    @Test
    public void constructorTest() {
        Contract contract = new Contract();
        assertNotNull(contract);
    }

    @Test
    public void fullArgsConstructorTest() {
        Contract contract = new Contract("Mihnea", "CSE1215",
                ZonedDateTime.now(), ZonedDateTime.now(), 100);
        assertNotNull(contract);
    }

    @Test
    public void generatePdfTest() {
        Contract contract = new Contract("Mihnea", "CSE1215",
                ZonedDateTime.now(), ZonedDateTime.now(), 100);
        assertDoesNotThrow(() -> contract.generate("name"));
    }
}
