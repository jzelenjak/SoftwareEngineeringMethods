package nl.tudelft.sem.authentication.entities;

import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDataTest {
    private static UserData userData;

    @BeforeEach
    void setUp() {
        userData = new UserData("jegor", "password1", UserRole.ADMIN);
    }

    @Test
    void getAuthoritiesTest() {
        Set<SimpleGrantedAuthority> expected = new HashSet<>();
        expected.add(new SimpleGrantedAuthority(UserRole.ADMIN.name()));

        assertEquals(expected, userData.getAuthorities());
    }

    @Test
    void getPasswordTest() {
        assertEquals("password1", userData.getPassword());
    }

    @Test
    void setPasswordTest() {
        // Cover empty constructor as well
        UserData user = new UserData();
        String before = user.getPassword();
        user.setPassword("password2");
        assertNotEquals(before, user.getPassword());
        assertEquals("password2", user.getPassword());
    }

    @Test
    void getUsernameTest() {
        assertEquals("jegor", userData.getUsername());
    }

    @Test
    void setUsernameTest() {
        String before = userData.getUsername();
        userData.setUsername("amogus");
        assertNotEquals(before, userData.getUsername());
        assertEquals("amogus", userData.getUsername());
    }

    @Test
    void getRoleTest() {
        assertEquals(UserRole.ADMIN, userData.getRole());
    }

    @Test
    void setRoleTest() {
        UserRole before = userData.getRole();
        userData.setRole(UserRole.TA);
        assertNotEquals(before, userData.getRole());
        assertEquals(UserRole.TA, userData.getRole());
    }

    @Test
    void isAccountNonExpiredTest() {
        assertTrue(userData.isAccountNonExpired());
    }

    @Test
    void setAccountNonExpiredTest() {
        boolean before = userData.isAccountNonExpired();
        userData.setAccountNonExpired(!before);
        assertEquals(!before, userData.isAccountNonExpired());
    }

    @Test
    void isAccountNonLockedTest() {
        assertTrue(userData.isAccountNonLocked());
    }

    @Test
    void setAccountNonLockedTest() {
        boolean before = userData.isAccountNonLocked();
        userData.setAccountNonLocked(!before);
        assertEquals(!before, userData.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpiredTest() {
        assertTrue(userData.isCredentialsNonExpired());
    }

    @Test
    void setCredentialsNonExpiredTest() {
        boolean before = userData.isCredentialsNonExpired();
        userData.setCredentialsNonExpired(!before);
        assertEquals(!before, userData.isCredentialsNonExpired());
    }

    @Test
    void isEnabledTest() {
        assertTrue(userData.isEnabled());
    }

    @Test
    void setEnabledTest() {
        boolean before = userData.isEnabled();
        userData.setEnabled(!before);
        assertEquals(!before, userData.isEnabled());
    }

    @Test
    void equalsSameTest() {
        assertEquals(userData, userData);
    }

    @Test
    void equalsEqualTest() {
        UserData otherUserData = new UserData("jegor", "password2", UserRole.TA);
        assertEquals(userData, otherUserData);
    }

    @Test
    void equalsDifferentTest() {
        UserData otherUserData = new UserData("jegorka", "password2", UserRole.LECTURER);
        assertNotEquals(userData, otherUserData);
    }

    @Test
    void hashCodeSameTest() {
        UserData otherUserData = new UserData("jegor", "password2", UserRole.TA);
        assertEquals(userData.hashCode(), otherUserData.hashCode());
    }

    @Test
    void hashCodeDifferentTest() {
        UserData otherUserData = new UserData("jegorka", "password2", UserRole.LECTURER);
        assertNotEquals(userData.hashCode(), otherUserData.hashCode());
    }
}