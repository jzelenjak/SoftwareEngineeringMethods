package nl.tudelft.sem.courses.unitTests;

import nl.tudelft.sem.courses.entities.Role;
import nl.tudelft.sem.courses.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testingConstructor(){
        User user = new User(1, Role.STUDENT);
        assertNotNull(user);
    }

    @Test
    public void testinigEqualsMethod(){
        User user = new User(1,  Role.STUDENT);
        User user2 = new User(2,  Role.STUDENT);
        User user3 = new User(1,  Role.STUDENT);
        User user4 = new User(3,  Role.LECTURER);



        assertEquals(user, user3);
        assertNotEquals(user, user2);
        assertNotEquals(user, user4);


    }


}
