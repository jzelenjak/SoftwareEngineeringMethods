package nl.tudelft.sem.hiring.procedure.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class AsyncAuthValidatorTest {

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @Test
    public void testConstructor() {
        AsyncAuthValidator validator = new AsyncAuthValidator(jwtUtils);
        assertNotNull(validator);
    }

    @Test
    public void testValidate() {
        when(jwtUtils.resolveToken("Bearer VALIDVALIDVALID")).thenReturn("VALIDVALIDVALID");
        when(jwtUtils.validateAndParseClaims("VALIDVALIDVALID")).thenReturn(jwsMock);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer VALIDVALIDVALID");
        AsyncAuthValidator validator = new AsyncAuthValidator(jwtUtils);

        Mono<Boolean> result = validator.validate(headers, "");

        assertEquals(Boolean.TRUE, result.block());
    }

    @Test
    public void testValidateNullAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        AsyncAuthValidator validator = new AsyncAuthValidator(jwtUtils);

        Mono<Boolean> result = validator.validate(headers, "");

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);
    }

    @Test
    public void testValidateIncorrectAuthorizationHeader() {
        when(jwtUtils.resolveToken("Wrong!")).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Wrong!");
        AsyncAuthValidator validator = new AsyncAuthValidator(jwtUtils);

        Mono<Boolean> result = validator.validate(headers, "");

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);
    }

    @Test
    public void testValidateInvalidAuthorizationHeader() {
        when(jwtUtils.resolveToken("Bearer FIDSFASDHFASDUFH")).thenReturn("FIDSFASDHFASDUFH");
        when(jwtUtils.validateAndParseClaims("FIDSFASDHFASDUFH")).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer FIDSFASDHFASDUFH");
        AsyncAuthValidator validator = new AsyncAuthValidator(jwtUtils);

        Mono<Boolean> result = validator.validate(headers, "");

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);
    }
}
