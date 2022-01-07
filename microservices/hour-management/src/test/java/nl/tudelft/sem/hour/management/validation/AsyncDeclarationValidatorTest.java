package nl.tudelft.sem.hour.management.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator.Roles;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class AsyncDeclarationValidatorTest {
    private static final String BEARER_TOKEN = "BEARER MyToken";
    private static final String AUTHORIZATION_TOKEN = "MyToken";

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @Mock
    private transient HttpHeaders httpHeaders;

    @BeforeEach
    void setupEach() {
        // Header mock to simulate authorization header
        when(httpHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);

        // Default behaviour for JWT utils library that always needs to be mocked
        when(jwtUtils.resolveToken(BEARER_TOKEN)).thenReturn(AUTHORIZATION_TOKEN);
        when(jwtUtils.validateAndParseClaims(AUTHORIZATION_TOKEN)).thenReturn(jwsMock);
    }

    @Test
    public void testConstructor() {
        // Configure request used for testing
        HourDeclarationRequest request = new HourDeclarationRequest(42, 1337, 10.5);

        // Construct validator instance
        AsyncDeclarationValidator validator = new AsyncDeclarationValidator(jwtUtils, request);

        // Assert that the validator must not be null
        assertNotNull(validator);
    }

    @Test
    public void testValidateAdmin() {
        // Set the role of the authorization token to admin
        mockRole(Roles.ADMIN);

        // Configure request used for testing
        HourDeclarationRequest request = new HourDeclarationRequest(42, 1337, 10.5);

        // Construct validator instance
        AsyncDeclarationValidator validator = new AsyncDeclarationValidator(jwtUtils, request);

        // Perform validation, admin should bypass all checks
        assertEquals(Boolean.TRUE, validator.validate(httpHeaders, "").block());
    }

    @Test
    public void testValidateStudent() {
        // Set the role of the authorization token to student and the user id
        long studentId = 42L;
        mockUserId(studentId);
        mockRole(Roles.STUDENT);

        // Configure request used for testing
        HourDeclarationRequest request = new HourDeclarationRequest(studentId, 1337, 10.5);

        // Construct validator instance
        AsyncDeclarationValidator validator = new AsyncDeclarationValidator(jwtUtils, request);

        // Perform validation, student should be permitted since the user id matches
        assertEquals(Boolean.TRUE, validator.validate(httpHeaders, "").block());
    }

    @Test
    public void testValidateStudentNotAllowedToDeclareForOtherUser() {
        // Set the role of the authorization token to student and a different user ID
        long studentId = 42L;
        mockUserId(Long.MAX_VALUE);
        mockRole(Roles.STUDENT);

        // Configure request used for testing
        HourDeclarationRequest request = new HourDeclarationRequest(studentId, 1337, 10.5);

        // Construct validator instance
        AsyncDeclarationValidator validator = new AsyncDeclarationValidator(jwtUtils, request);

        // Validate the request and store the result
        Mono<Boolean> result = validator.validate(httpHeaders, "");

        // Perform validation, student should not be allowed to declare hours for other user
        assertThrows(ResponseStatusException.class, result::block);
    }

    /**
     * Utility function to mock the role of an authorization token.
     *
     * @param role the role to set in the mock.
     */
    private void mockRole(Roles role) {
        when(jwtUtils.getRole(jwsMock)).thenReturn(role.name());
    }

    /**
     * Utility function to mock the user ID of an authorization token.
     *
     * @param userId the user ID to set in the mock.
     */
    private void mockUserId(Long userId) {
        when(jwtUtils.getUserId(jwsMock)).thenReturn(userId);
    }

}
