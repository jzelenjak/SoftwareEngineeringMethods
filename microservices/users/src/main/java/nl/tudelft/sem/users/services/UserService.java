package nl.tudelft.sem.users.services;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A class that represents a User Service.
 */
@Service
@Transactional
public class UserService {

    private final transient UserRepository userRepository;

    /**
     * Instantiates a new UserService object.
     *
     * @param userRepository the user repository
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Registers a new user if (s)he does not already exist and if the provided data are valid.
     *      The role is initially set to STUDENT for all users.
     * Regarding TA's feedback on many if statements: unfortunately since Java doesn't allow
     *   switch statements on multiple variables, I could not fully incorporate the feedback.
     *   However, I tried to refactor the method a bit, and I think this is more clean.
     *
     * @param username      the username of the user
     * @param firstName     the name of the user
     * @param lastName      the surname of the user
     * @return the user ID for the new user, if the operation has been successful
     * @throws DataIntegrityViolationException if the username already exists
     *          and if any of the username, first name or last name are blank or null
     */
    public long registerUser(String username, String firstName, String lastName)
            throws DataIntegrityViolationException {

        if (nullOrBlank(username)) {
            throw new DataIntegrityViolationException("Please specify the username!");
        }

        if (this.userRepository.findByUsername(username).isPresent()) {
            String msg = String.format("User with username %s already exist", username);
            throw new DataIntegrityViolationException(msg);
        }

        if (nullOrBlank(firstName) || nullOrBlank(lastName)) {
            throw new DataIntegrityViolationException("Please specify the first and last name!");
        }

        return this.userRepository
            .save(new User(username, firstName, lastName, UserRole.STUDENT)).getUserId();
    }


    /**
     * Gets the user by his/her username.
     * Only allowed for lecturers and admins which is checked in UserController.
     *
     * @param username         the username of the user
     * @return the user with the provided username if found
     */
    public Optional<User> getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }


    /**
     * Gets the user by his/her user ID.
     * Only allowed for lecturers and admins which is checked in UserController.
     *
     * @param userId        the user ID
     * @return the user with the provided user ID if found
     */
    public Optional<User> getUserByUserId(long userId) {
        return this.userRepository.findByUserId(userId);
    }


    /**
     * Gets users by their role.
     * Only allowed for lecturers and admins which is checked in UserController.
     *
     * @param role          the role of a user
     * @return the users who have the given role
     */
    public List<User> getUsersByRole(UserRole role) {
        return this.userRepository.findAllByRole(role);
    }


    /**
     * Changes the role of a user to another role.
     * Only allowed for admins which is checked in UserController.
     *
     * @param userId        the user ID of the user
     * @param newRole       the new role of the user
     * @return true if the operation has been successful, false if the user does not exist
     */
    public boolean changeRole(long userId, UserRole newRole) {
        Optional<User> optionalUser = this.userRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setRole(newRole);
        this.userRepository.save(user);
        return true;
    }

    /**
     * Deletes the user by their username.
     * Only allowed for admins which is checked in UserController.
     *
     * @param userId        the user ID of the user
     * @return true if the operation has been successful, false if the user does not exist
     */
    public boolean deleteUserByUserId(long userId) {
        if (this.userRepository.findByUserId(userId).isEmpty()) {
            return false;
        }
        this.userRepository.deleteByUserId(userId);
        return true;
    }

    /**
     * A helper method that checks whether a string is null or blank.
     *
     * @param str   the string to check
     * @return true if the string is null or blank, false otherwise
     */
    private boolean nullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
