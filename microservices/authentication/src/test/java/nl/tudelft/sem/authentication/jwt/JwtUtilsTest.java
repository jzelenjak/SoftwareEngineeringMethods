package nl.tudelft.sem.authentication.jwt;

import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {
    @Autowired
    @Qualifier("secretKey")
    private transient Key hmacKey;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(hmacKey);
    }

    @Test
    void createAndResolveValidTokenTest() {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 10);

        Date date = new Date();
        String jwt = jwtUtils.createToken("amogus", UserRole.STUDENT, date);
        String jwtPrefixed = "Bearer " + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        assertTrue(jwtUtils.validateToken(tokenBody),
                "Invalid or expired token");
        assertEquals("amogus", jwtUtils.getUsername(tokenBody),
                "Decoded username does not match the original one");
        assertEquals(UserRole.STUDENT.name(), jwtUtils.getRole(tokenBody),
                "Decoded role does not match the original one");
    }



    @Test
    void createAndResolveInValidTokenExpiredTest() {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 10);

        Date date = new Date(new Date().getTime() - 10 * 60000);
        String jwt = jwtUtils.createToken("amogus", UserRole.STUDENT, date);
        String jwtPrefixed = "Bearer " + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
    }

    @Test
    void createAndResolveInValidTokenZeroValidityTest() {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 0);

        Date date = new Date();
        String jwt = jwtUtils.createToken("amogus", UserRole.STUDENT, date);
        String jwtPrefixed = "Bearer " + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
    }

    @Test
    void resolveTokenNullTest() {
        assertNull(jwtUtils.resolveToken(null));
    }

    @Test
    void resolveTokenNotStartsWithBearerTest() {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 0);
        String jwt = jwtUtils.createToken("amogus", UserRole.ADMIN, new Date());

        assertNull(jwtUtils.resolveToken(jwtUtils.resolveToken(jwt)));
    }
}