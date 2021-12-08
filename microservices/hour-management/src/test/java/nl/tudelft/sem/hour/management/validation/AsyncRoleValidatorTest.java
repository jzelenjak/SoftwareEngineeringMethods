package nl.tudelft.sem.hour.management.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Set;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator.Roles;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeAll;
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
    private static GatewayConfig gatewayConfig;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private Jws<Claims> jwsMock;

    @BeforeAll
    static void setup(){
        gatewayConfig = new GatewayConfig();
        gatewayConfig.setHost("");
        gatewayConfig.setPort(0);
    }

    @Test
    public void testConstructor() {
        AsyncRoleValidator validator = new AsyncRoleValidator(gatewayConfig, jwtUtils);
        assertNotNull(validator);

        assertEquals(validator.getAuthorizedRoles(), Set.of(Roles.ADMIN));
    }

    @Test
    public void testConstructorProvideSet() {
        AsyncRoleValidator validator = new AsyncRoleValidator(gatewayConfig, jwtUtils, Set.of(Roles.TA));
        assertNotNull(validator);

        assertEquals(validator.getAuthorizedRoles(), Set.of(Roles.TA));
    }

    @Test
    public void testValidate() {
        when(jwtUtils.resolveToken("Bearer VALIDVALIDVALID")).thenReturn("VALIDVALIDVALID");
        when(jwtUtils.validateAndParseClaims("VALIDVALIDVALID")).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn("TA");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer VALIDVALIDVALID");
        AsyncRoleValidator validator = new AsyncRoleValidator(gatewayConfig, jwtUtils, Set.of(Roles.ADMIN, Roles.TA));

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
        AsyncRoleValidator validator = new AsyncRoleValidator(gatewayConfig, jwtUtils, Set.of(Roles.ADMIN, Roles.TA));

        Mono<Boolean> result = validator.validate(headers, "");

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);
    }
}
