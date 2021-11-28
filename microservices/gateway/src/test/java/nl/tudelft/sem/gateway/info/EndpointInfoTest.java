package nl.tudelft.sem.gateway.info;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.assertj.core.api.Assertions.*;

public class EndpointInfoTest {
    @Test
    void testValidConstructor() {
        assertThatCode(() -> new EndpointInfo("/", HttpMethod.GET)).doesNotThrowAnyException();
    }

    @Test
    void testNullPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new EndpointInfo(null, HttpMethod.GET));
    }

    @Test
    void testBlankPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new EndpointInfo("    ", HttpMethod.GET));
    }

    @Test
    void testNullMethod() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new EndpointInfo("/", null));
    }
}
