package nl.tudelft.sem.authentication.entities;

import java.util.Set;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


class UserDataTest {
    private transient UserData userData;
    private final transient String username = "jegor";
    private final transient String password1 = "password1";
    private final transient String password2 = "password2";
    private final transient long userId = 1234567L;

    @BeforeEach
    void setUp() {
        userData = new UserData(username, password1, UserRole.ADMIN, userId);
    }

    @Test
    void testGetAuthorities() {
        Set<SimpleGrantedAuthority> expected = Set
                .of(new SimpleGrantedAuthority(UserRole.ADMIN.name()));
        Assertions.assertEquals(expected, userData.getAuthorities());
    }

    @Test
    void testGetPassword() {
        Assertions.assertEquals(password1, userData.getPassword());
    }

    @Test
    void testSetPassword() {
        // Cover empty constructor as well
        UserData user = new UserData();
        user.setPassword(password2);
        Assertions.assertEquals(password2, user.getPassword());
    }

    @Test
    void testGetUsername() {
        Assertions.assertEquals(username, userData.getUsername());
    }

    @Test
    void testSetUsername() {
        UserData user = new UserData();
        user.setUsername("amogus");
        Assertions.assertEquals("amogus", user.getUsername());
    }

    @Test
    void testGetUserId() {
        Assertions.assertEquals(userId, userData.getUserId());
    }

    @Test
    void testSetUserId() {
        UserData user = new UserData();
        user.setUserId(userId);
        Assertions.assertEquals(userId, user.getUserId());
    }

    @Test
    void testGetRole() {
        Assertions.assertEquals(UserRole.ADMIN, userData.getRole());
    }

    @Test
    void testSetRole() {
        UserData user = new UserData();
        user.setRole(UserRole.LECTURER);
        Assertions.assertEquals(UserRole.LECTURER, user.getRole());
    }

    @Test
    void testIsAccountNonExpired() {
        Assertions.assertTrue(userData.isAccountNonExpired());
    }

    @Test
    void testSetAccountNonExpired() {
        boolean before = userData.isAccountNonExpired();
        userData.setAccountNonExpired(!before);
        Assertions.assertEquals(!before, userData.isAccountNonExpired());

        userData.setAccountNonExpired(before);
    }

    @Test
    void testIsAccountNonLocked() {
        Assertions.assertTrue(userData.isAccountNonLocked());
    }

    @Test
    void testSetAccountNonLocked() {
        boolean before = userData.isAccountNonLocked();
        userData.setAccountNonLocked(!before);
        Assertions.assertEquals(!before, userData.isAccountNonLocked());

        userData.setAccountNonLocked(before);
    }

    @Test
    void testIsCredentialsNonExpired() {
        Assertions.assertTrue(userData.isCredentialsNonExpired());
    }

    @Test
    void testSetCredentialsNonExpired() {
        boolean before = userData.isCredentialsNonExpired();
        userData.setCredentialsNonExpired(!before);
        Assertions.assertEquals(!before, userData.isCredentialsNonExpired());

        userData.setCredentialsNonExpired(before);
    }

    @Test
    void testIsEnabled() {
        Assertions.assertTrue(userData.isEnabled());
    }

    @Test
    void testSetEnabled() {
        boolean before = userData.isEnabled();
        userData.setEnabled(!before);
        Assertions.assertEquals(!before, userData.isEnabled());

        userData.setEnabled(before);
    }

    @Test
    void testEqualsSame() {
        Assertions.assertEquals(userData, userData);
    }

    @Test
    void testEqualsOtherObject() {
        Assertions.assertNotEquals(userData, "userData");
    }

    @Test
    void testEqualsEqual() {
        UserData otherUserData = new UserData(username, password2, UserRole.LECTURER, userId);
        Assertions.assertEquals(userData, otherUserData);
    }

    @Test
    void testEqualsDifferent() {
        UserData otherUserData = new UserData("jegorka", password2, UserRole.LECTURER, userId);
        Assertions.assertNotEquals(userData, otherUserData);
    }

    @Test
    void testHashCodeSame() {
        UserData otherUserData = new UserData(username, password2, UserRole.ADMIN, userId);
        Assertions.assertEquals(userData.hashCode(), otherUserData.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        UserData otherUserData = new UserData("jegorkaboom", password2, UserRole.LECTURER, userId);
        Assertions.assertNotEquals(userData.hashCode(), otherUserData.hashCode());
    }
}