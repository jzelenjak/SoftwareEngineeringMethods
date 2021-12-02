package nl.tudelft.sem.authentication.entities;

import java.util.HashSet;
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

    @BeforeEach
    void setUp() {
        userData = new UserData(username, password1, UserRole.ADMIN);
    }

    @Test
    void getAuthoritiesTest() {
        Set<SimpleGrantedAuthority> expected = new HashSet<>();
        expected.add(new SimpleGrantedAuthority(UserRole.ADMIN.name()));

        Assertions.assertEquals(expected, userData.getAuthorities());
    }

    @Test
    void getPasswordTest() {
        Assertions.assertEquals(password1, userData.getPassword());
    }

    @Test
    void setPasswordTest() {
        // Cover empty constructor as well
        UserData user = new UserData();
        String before = user.getPassword();
        user.setPassword(password2);
        Assertions.assertNotEquals(before, user.getPassword());
        Assertions.assertEquals(password2, user.getPassword());
    }

    @Test
    void getUsernameTest() {
        Assertions.assertEquals(username, userData.getUsername());
    }

    @Test
    void setUsernameTest() {
        String before = userData.getUsername();
        userData.setUsername("amogus");
        Assertions.assertNotEquals(before, userData.getUsername());
        Assertions.assertEquals("amogus", userData.getUsername());
    }

    @Test
    void getRoleTest() {
        Assertions.assertEquals(UserRole.ADMIN, userData.getRole());
    }

    @Test
    void setRoleTest() {
        UserRole before = userData.getRole();
        userData.setRole(UserRole.TA);
        Assertions.assertNotEquals(before, userData.getRole());
        Assertions.assertEquals(UserRole.TA, userData.getRole());
    }

    @Test
    void isAccountNonExpiredTest() {
        Assertions.assertTrue(userData.isAccountNonExpired());
    }

    @Test
    void setAccountNonExpiredTest() {
        boolean before = userData.isAccountNonExpired();
        userData.setAccountNonExpired(!before);
        Assertions.assertEquals(!before, userData.isAccountNonExpired());
    }

    @Test
    void isAccountNonLockedTest() {
        Assertions.assertTrue(userData.isAccountNonLocked());
    }

    @Test
    void setAccountNonLockedTest() {
        boolean before = userData.isAccountNonLocked();
        userData.setAccountNonLocked(!before);
        Assertions.assertEquals(!before, userData.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpiredTest() {
        Assertions.assertTrue(userData.isCredentialsNonExpired());
    }

    @Test
    void setCredentialsNonExpiredTest() {
        boolean before = userData.isCredentialsNonExpired();
        userData.setCredentialsNonExpired(!before);
        Assertions.assertEquals(!before, userData.isCredentialsNonExpired());
    }

    @Test
    void isEnabledTest() {
        Assertions.assertTrue(userData.isEnabled());
    }

    @Test
    void setEnabledTest() {
        boolean before = userData.isEnabled();
        userData.setEnabled(!before);
        Assertions.assertEquals(!before, userData.isEnabled());
    }

    @Test
    void equalsSameTest() {
        Assertions.assertEquals(userData, userData);
    }

    @Test
    void equalsOtherObjectTest() {
        Assertions.assertNotEquals(userData, "userData");
    }

    @Test
    void equalsEqualTest() {
        UserData otherUserData = new UserData(username, password2, UserRole.TA);
        Assertions.assertEquals(userData, otherUserData);
    }

    @Test
    void equalsDifferentTest() {
        UserData otherUserData = new UserData("jegorka", password2, UserRole.LECTURER);
        Assertions.assertNotEquals(userData, otherUserData);
    }

    @Test
    void hashCodeSameTest() {
        UserData otherUserData = new UserData(username, password2, UserRole.TA);
        Assertions.assertEquals(userData.hashCode(), otherUserData.hashCode());
    }

    @Test
    void hashCodeDifferentTest() {
        UserData otherUserData = new UserData("jegorka", password2, UserRole.LECTURER);
        Assertions.assertNotEquals(userData.hashCode(), otherUserData.hashCode());
    }
}