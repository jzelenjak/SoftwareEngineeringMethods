package nl.tudelft.sem.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class JwtUtilsTest {
    private final transient String secretKeyString =
            "correcthorsebatterystaplecorrecthorsebatterystaple";
    private final transient JwtUtils jwtUtils = new JwtUtils(secretKeyString);
    private final transient Key secretKey = new SecretKeySpec(secretKeyString.getBytes(),
            SignatureAlgorithm.HS256.getJcaName());

    private final transient long userId = 5465321L;


    /**
     * A helper method to create a JWT token.
     *
     * @param userId            the user ID of the user
     * @param role              the role of the user
     * @param date              the date the token has been issued on
     * @param validityInMinutes the validity of the token in minutes
     * @return the created and signed JWT token
     */
    private String createToken(long userId, String role, Date date, long validityInMinutes) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);
        Date validity = new Date(date.getTime() + validityInMinutes * 60000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(date)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }


    @Test
    void resolveTokenNoPrefixTest() {
        String jwt = createToken(userId - 1L, "STUDENT", new Date(), 10);

        Assertions
            .assertThat(jwtUtils.resolveToken("Bear " + jwt))
            .isNull();
    }

    @Test
    void resolveTokenNullTest() {
        Assertions
            .assertThat(jwtUtils.resolveToken(null))
            .isNull();
    }

    @Test
    void resolveTokenCorrectTest() {
        String jwt = createToken(userId, "TA", new Date(), 11);

        Assertions
            .assertThat(jwtUtils.resolveToken("Bearer " + jwt))
            .isEqualTo(jwt);
    }

    @Test
    void validateAndParseClaimsExpiredTest() {
        String jwt = createToken(userId, "STUDENT", new Date(), 0);

        Assertions
            .assertThat(jwtUtils.validateAndParseClaims(jwt))
            .isNull();
    }

    @Test
    void validateAndParseClaimsCorruptedTest() {
        String jwt = createToken(userId, "LECTURER", new Date(), 20);

        Assertions
            .assertThat(jwtUtils.validateAndParseClaims("sus" + jwt))
            .isNull();
    }

    @Test
    void validateAndParseValidTest() {
        String jwt = createToken(userId, "CANDIDATE_TA", new Date(), 15);

        Assertions
            .assertThat(jwtUtils.validateAndParseClaims(jwt))
            .isInstanceOf(Jws.class);
    }

    @Test
    void getUserIdTest() {
        String jwt = createToken(userId, "TA", new Date(), 5);
        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(jwt);

        Assertions
            .assertThat(jwtUtils.getUserId(claimsJws))
            .isEqualTo(userId);
    }

    @Test
    void getRoleTest() {
        String jwt = createToken(userId, "ADMIN", new Date(), 40);
        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(jwt);

        Assertions
            .assertThat(jwtUtils.getRole(claimsJws))
            .isEqualTo("ADMIN");
    }
}