package nl.tudelft.sem.authentication.service;

import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A class that represents Service which communicates with the database containing the user details.
 */
@Service
public class AuthService implements UserDetailsService {

    private final transient UserDataRepository userDataRepository;

    private final transient PasswordEncoder passwordEncoder;

    /**
     * Constructor for the AuthService.
     *
     * @param userDataRepository the repository containing the user data.
     * @param passwordEncoder    the password encoder.
     * @param rootUsername       the username of the root user.
     * @param rootPassword       the password of the root user.
     * @param rootUserId         the user ID of the root user.
     */
    public AuthService(UserDataRepository userDataRepository, PasswordEncoder passwordEncoder,
                       @Value("${root.username}") String rootUsername,
                       @Value("${root.password}") String rootPassword,
                       @Value("${root.userid}") long rootUserId) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;

        if (this.userDataRepository.findByUsername(rootUsername).isEmpty()) {
            this.userDataRepository.save(
                    new UserData(rootUsername, this.passwordEncoder.encode(rootPassword),
                            UserRole.ADMIN, rootUserId));
        }
    }

    /**
     * Registers a new user into the database.
     *
     * @param username the username of the new user.
     * @param userId   the user ID of the new user.
     * @param password the password of the new user.
     * @return true if the registration has been successful,
     *         false otherwise (if the user with the same username/userid already exists).
     */
    public boolean registerUser(String username, long userId, String password) {
        // Check if user with given name or userId already exists.
        boolean usernameCheck = this.userDataRepository.findByUsername(username).isPresent();
        boolean userIdCheck = this.userDataRepository.findByUserId(userId).isPresent();
        if (usernameCheck || userIdCheck) {
            return false;
        }
        this.userDataRepository
                .save(new UserData(username, passwordEncoder.encode(password),
                        UserRole.STUDENT, userId));
        return true;
    }

    /**
     * Changes the password of an existing user.
     *
     * @param username    the username of the existing user.
     * @param newPassword the new password for this user.
     */
    public void changePassword(String username, String newPassword) {
        UserData userData = loadUserByUsername(username);
        userData.setPassword(passwordEncoder.encode(newPassword));
        userDataRepository.save(userData);
    }

    /**
     * Finds the user by their username.
     *
     * @param username the username of the user to be found.
     * @return the user object, if it is found in the repository.
     * @throws UsernameNotFoundException thrown when the user has not been found.
     */
    @Override
    public UserData loadUserByUsername(String username) {
        return this.userDataRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(
                        "User with username %s not found", username)));
    }

    /**
     * Finds the user by their user ID.
     *
     * @param userId the user ID of the user to be found.
     * @return the user object, if it is found in the repository.
     * @throws UsernameNotFoundException thrown when the user has not been found.
     */
    public UserData loadUserByUserId(long userId) {
        return this.userDataRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String
                                .format("User with user ID %s not found", userId)));
    }

    /**
     * Change role of the user.
     *
     * @param username the username of the user.
     * @param newRole  the new role of the user.
     */
    public void changeRole(String username, UserRole newRole) {
        UserData userData = loadUserByUsername(username);
        userData.setRole(newRole);
        userDataRepository.save(userData);
    }

    /**
     * Delete user specified by the username.
     *
     * @param username the username of the to be removed user.
     */
    public void deleteUser(String username) {
        UserData userData = loadUserByUsername(username);
        userDataRepository.delete(userData);
    }
}
