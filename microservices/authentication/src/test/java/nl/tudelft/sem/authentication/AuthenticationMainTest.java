package nl.tudelft.sem.authentication;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A test class to cover the AuthenticationMain.java class.
 */
public class AuthenticationMainTest {
    @Test
    public void main() {
        Assertions
                .assertThatCode(() -> AuthenticationMain.main(new String[]{"amogus"}))
                .doesNotThrowAnyException();
    }
}