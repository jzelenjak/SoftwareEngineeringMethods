package nl.tudelft.sem.hiring.procedure.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Set;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator.Roles;
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
public class AsyncRoleValidatorTest {

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @Test
    public void testConstructor() {
        AsyncRoleValidator validator = new AsyncRoleValidator(jwtUtils);
        assertNotNull(validator);

        assertEquals(validator.getAuthorizedRoles(), Set.of(Roles.ADMIN));
    }

    @Test
    public void testConstructorProvideSet() {
        AsyncRoleValidator validator =
                new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT));
        assertNotNull(validator);

        assertEquals(validator.getAuthorizedRoles(), Set.of(Roles.STUDENT));
    }

    @Test
    public void testValidate() {
        when(jwtUtils.resolveToken("Bearer VALIDVALIDVALID")).thenReturn("VALIDVALIDVALID");
        when(jwtUtils.validateAndParseClaims("VALIDVALIDVALID")).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn("STUDENT");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer VALIDVALIDVALID");
        AsyncRoleValidator validator =
                new AsyncRoleValidator(jwtUtils, Set.of(Roles.ADMIN, Roles.STUDENT));

        Mono<Boolean> result = validator.validate(headers, "");

        assertEquals(Boolean.TRUE, result.block());
    }

    @Test
    public void testValidateInvalidRole() {
        when(jwtUtils.resolveToken("Bearer INVALIDINVALID")).thenReturn("INVALIDINVALID");
        when(jwtUtils.validateAndParseClaims("INVALIDINVALID")).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn("STUDENT");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer INVALIDINVALID");
        AsyncRoleValidator validator = new AsyncRoleValidator(jwtUtils, Set.of(Roles.ADMIN));

        Mono<Boolean> result = validator.validate(headers, "");

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);
    }
}
