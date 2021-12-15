package nl.tudelft.sem.hiring.procedure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HiringProcedureMainTest {
    @Test
    public void applicationContextTest() {
        HiringProcedureMain.main(new String[] {});
    }
}
