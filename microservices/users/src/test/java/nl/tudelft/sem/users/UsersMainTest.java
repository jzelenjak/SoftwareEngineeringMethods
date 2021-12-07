package nl.tudelft.sem.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A test class to cover the UsersMain.java class.
 */
public class UsersMainTest {
    @Test
    public void main() {
        Assertions
            .assertThatCode(() -> UsersMain.main(new String[]{"impostor"}))
            .doesNotThrowAnyException();
    }
}
