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
    void getAuthorities() {
        Set<SimpleGrantedAuthority> expected = new HashSet<>();
        expected.add(new SimpleGrantedAuthority(UserRole.ADMIN.name()));

        assertEquals(expected, userData.getAuthorities());
    }

    @Test
    void getPassword() {
        assertEquals("password1", userData.getPassword());
    }

    @Test
    void setPassword() {
        // Cover empty constructor as well
        UserData user = new UserData();
        String before = user.getPassword();
        user.setPassword("password2");
        assertNotEquals(before, user.getPassword());
        assertEquals("password2", user.getPassword());
    }

    @Test
    void getUsername() {
        assertEquals("jegor", userData.getUsername());
    }

    @Test
    void setUsername() {
        String before = userData.getUsername();
        userData.setUsername("amogus");
        assertNotEquals(before, userData.getUsername());
        assertEquals("amogus", userData.getUsername());
    }

    @Test
    void getRole() {
        assertEquals(UserRole.ADMIN, userData.getRole());
    }

    @Test
    void setRole() {
        UserRole before = userData.getRole();
        userData.setRole(UserRole.TA);
        assertNotEquals(before, userData.getRole());
        assertEquals(UserRole.TA, userData.getRole());
    }

    @Test
    void isAccountNonExpired() {
        assertTrue(userData.isAccountNonExpired());
    }

    @Test
    void setAccountNonExpired() {
        boolean before = userData.isAccountNonExpired();
        userData.setAccountNonExpired(!before);
        assertEquals(!before, userData.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked() {
        assertTrue(userData.isAccountNonLocked());
    }

    @Test
    void setAccountNonLocked() {
        boolean before = userData.isAccountNonLocked();
        userData.setAccountNonLocked(!before);
        assertEquals(!before, userData.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired() {
        assertTrue(userData.isCredentialsNonExpired());
    }

    @Test
    void setCredentialsNonExpired() {
        boolean before = userData.isCredentialsNonExpired();
        userData.setCredentialsNonExpired(!before);
        assertEquals(!before, userData.isCredentialsNonExpired());
    }

    @Test
    void isEnabled() {
        assertTrue(userData.isEnabled());
    }

    @Test
    void setEnabled() {
        boolean before = userData.isEnabled();
        userData.setEnabled(!before);
        assertEquals(!before, userData.isEnabled());
    }

    @Test
    void equalsSame() {
        assertEquals(userData, userData);
    }

    @Test
    void equalsEqual() {
        UserData otherUserData = new UserData("jegor", "password2", UserRole.TA);
        assertEquals(userData, otherUserData);
    }

    @Test
    void equalsDifferent() {
        UserData otherUserData = new UserData("jegorka", "password2", UserRole.LECTURER);
        assertNotEquals(userData, otherUserData);
    }

    @Test
    void hashCodeSame() {
        UserData otherUserData = new UserData("jegor", "password2", UserRole.TA);
        assertEquals(userData.hashCode(), otherUserData.hashCode());
    }

    @Test
    void hashCodeDifferent() {
        UserData otherUserData = new UserData("jegorka", "password2", UserRole.LECTURER);
        assertNotEquals(userData.hashCode(), otherUserData.hashCode());
    }
}