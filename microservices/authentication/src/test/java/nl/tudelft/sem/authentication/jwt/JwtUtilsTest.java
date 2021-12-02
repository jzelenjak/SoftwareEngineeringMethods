package nl.tudelft.sem.authentication.jwt;

import java.security.Key;
import java.util.Date;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;


@SpringBootTest
class JwtUtilsTest {
    @Autowired
    @Qualifier("secretKey")
    private transient Key hmacKey;

    private transient JwtUtils jwtUtils;

    private final transient String prefix = "Bearer ";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(hmacKey);
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 10);
    }

    @Test
    void createAndResolveValidTokenTest() {
        Date date = new Date();
        String jwt = jwtUtils.createToken("amogus", UserRole.STUDENT, date);
        String jwtPrefixed = prefix + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        Assertions.assertFalse(tokenBody.startsWith(prefix),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertTrue(jwtUtils.validateToken(tokenBody),
                "Invalid or expired token");
        Assertions.assertEquals("amogus", jwtUtils.getUsername(tokenBody),
                "Decoded username does not match the original one");
        Assertions.assertEquals(UserRole.STUDENT.name(), jwtUtils.getRole(tokenBody),
                "Decoded role does not match the original one");
    }



    @Test
    void createAndResolveInValidTokenExpiredTest() {

        Date date = new Date(new Date().getTime() - 10 * 60000);
        String jwt = jwtUtils.createToken("amongus", UserRole.STUDENT, date);
        String jwtPrefixed = prefix + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        Assertions.assertFalse(tokenBody.startsWith(prefix),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
    }

    @Test
    void createAndResolveInValidTokenZeroValidityTest() {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 0);

        Date date = new Date();
        String jwt = jwtUtils.createToken("amongus", UserRole.STUDENT, date);
        String jwtPrefixed = prefix + jwt;

        String tokenBody = jwtUtils.resolveToken(jwtPrefixed);

        Assertions.assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
    }

    @Test
    void resolveTokenNullTest() {
        Assertions.assertNull(jwtUtils.resolveToken(null));
    }

    @Test
    void resolveTokenNotStartsWithBearerTest() {
        String jwt = jwtUtils.createToken("amog us", UserRole.ADMIN, new Date());

        Assertions.assertNull(jwtUtils.resolveToken(jwtUtils.resolveToken(jwt)));
    }
}