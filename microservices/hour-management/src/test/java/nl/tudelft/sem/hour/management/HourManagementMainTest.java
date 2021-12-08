package nl.tudelft.sem.hour.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HourManagementMainTest {
    @Test
    public void applicationContextTest() {
        HourManagementMain.main(new String[] {});
    }
}
