package nl.tudelft.sem.entities.services;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import nl.tudelft.sem.entities.repositories.UserRepository;
import org.springframework.stereotype.Service;

/**
 * A class that represents a User Service.
 */
@Service
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
     * Registers a new user if (s)he does not already exist.
     *
     * @param netId         the netID (username) of the user
     * @param firstName     the name of the user
     * @param lastName      the surname of the user
     * @param role          the role of the user
     * @return true is a user has been successfully registered;
     *         false if a user with the same netID already exists
     */
    public long registerUser(String netId, String firstName, String lastName, UserRole role) {
        if (this.userRepository.findByUsername(netId).isPresent()) {
            return -1;
        }
        this.userRepository.save(new User(netId, firstName, lastName, role));

        Optional<User> user = this.userRepository.findByUsername(netId);
        assert user.isPresent();
        return user.get().getUserId();
    }

    /**
     * Gets the user by his/her netID.
     *
     * @param netId the netID
     * @return the user with the provided netID if found
     */
    public Optional<User> getUserByNetId(String netId) {
        return this.userRepository.findByUsername(netId);
    }

    /**
     * Gets the user by his/her userID.
     *
     * @param userId the userID
     * @return the user with the provided userID if found
     */
    public Optional<User> getUserByUserId(long userId) {
        return this.userRepository.findByUserId(userId);
    }
    /**
     * Gets users by their role.
     *
     * @param role the role of a user
     * @return the users who have the given role
     */
    public List<User> getUsersByRole(UserRole role) {
        return this.userRepository.findAllByRole(role);
    }
}
