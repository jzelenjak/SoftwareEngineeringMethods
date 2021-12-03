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
     * @return true is a user has been successfully registered;
     *         false if a user with the same netID already exists
     */
    public long registerUser(String netId, String firstName, String lastName) {
        if (this.userRepository.findByUsername(netId).isPresent()) {
            return -1;
        }
        this.userRepository.save(new User(netId, firstName, lastName, UserRole.STUDENT));

        Optional<User> userFromRepo = this.userRepository.findByUsername(netId);
        assert userFromRepo.isPresent();
        return userFromRepo.get().getUserId();
    }

    /**
     * Gets the user by his/her netID.
     *
     * @param netId         the netID
     * @return the user with the provided netID if found
     */
    public Optional<User> getUserByNetId(String netId) {
        return this.userRepository.findByUsername(netId);
    }

    /**
     * Gets the user by his/her userID.
     *
     * @param userId        the userID
     * @return the user with the provided userID if found
     */
    public Optional<User> getUserByUserId(long userId) {
        return this.userRepository.findByUserId(userId);
    }

    /**
     * Gets users by their role.
     *
     * @param role          the role of a user
     * @return the users who have the given role
     */
    public List<User> getUsersByRole(UserRole role) {
        return this.userRepository.findAllByRole(role);
    }

    /**
     * Changes the role of a user to another role if the requester has permissions for that.
     *
     * @param netId         the netID of the user
     * @param newRole       the new role of the user
     * @param requesterRole the role of the requester
     * @return true if the operation has been successful,
     *         false if the requester does not have the required permissions
     */
    public boolean changeRole(String netId, UserRole newRole, UserRole requesterRole) {
        // Only admins and lecturers can change permissions
        if (!requesterRole.equals(UserRole.ADMIN) && !requesterRole.equals(UserRole.LECTURER)) {
            return false;
        }

        // Only an admin can make someone an admin
        if (newRole.equals(UserRole.ADMIN) && !requesterRole.equals(UserRole.ADMIN)) {
            return false;
        }

        // Only an admin can make someone a lecturer
        if (newRole.equals(UserRole.LECTURER) && !requesterRole.equals(UserRole.ADMIN)) {
            return false;
        }

        // Both lecturers and admins can make someone else a TA, CANDIDATE_TA or STUDENT

        Optional<User> optionalUser = this.userRepository.findByUsername(netId);
        assert optionalUser.isPresent();

        User user = optionalUser.get();
        user.setRole(newRole);
        this.userRepository.save(user);
        return true;
    }

    /**
     * Deletes the user by their username (netID).
     *
     * @param netId         the username (netID) of the user
     * @param requesterRole the role of the requester
     * @return true if the operation has been successful,
     *         false if the requester does not have the required permissions (is not an admin)
     */
    public boolean deleteUserByUsername(String netId, UserRole requesterRole) {
        if (!requesterRole.equals(UserRole.ADMIN)) {
            return false;
        }

        this.userRepository.deleteByUsername(netId);
        return true;
    }
}
