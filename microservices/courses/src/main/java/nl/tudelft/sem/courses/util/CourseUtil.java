package nl.tudelft.sem.courses.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseUtil {

    private final transient JwtUtils jwtUtils;

    /**
     * Method for taking in a jwt token and authorizing it for the user.
     *
     * @param httpHeaders - the header of a http request
     * @return - returns true if authenticated otherwise returns false
     */
    public Jws<Claims> isAuthorized(HttpHeaders httpHeaders) {
        //first we try to get the authorization header information.
        String authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        //if there is no such header return null
        if (authHeader == null) {
            return null;
        }
        //we create a new token
        String token = jwtUtils.resolveToken(authHeader);
        if (token == null) {
            return null;
        }
        //a Json webToken containing the parsed JWS claims
        return jwtUtils.validateAndParseClaims(token);
    }

    /**
     * Method checks if the role in webToken is student.
     *
     * @param claimsJws - a webToken
     * @return - true if student/ta else false
     */
    public boolean checkIfStudent(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("STUDENT");
    }

    /**
     * Method checks if role in webToken is a lecturer or an admin.
     *
     * @param claimsJws - a webToken
     * @return - true if lecturer/admin, false otherwise
     */
    public boolean checkIfLecturerOrAdmin(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("LECTURER") || role.equals("ADMIN");
    }

    /**
     * Method checks if role in web token is
     * admin.
     *
     * @param claimsJws - a web token
     * @return - true if admin else false
     */
    public boolean checkIfAdmin(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("ADMIN");
    }

    /**
     * Method checks if role in web token is
     * lecturer.
     *
     * @param claimsJws - a web token
     * @return - true if lecturer else false
     */
    public boolean checkIfLecturer(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("LECTURER");
    }
}
