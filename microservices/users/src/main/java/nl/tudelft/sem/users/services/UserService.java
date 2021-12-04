package nl.tudelft.sem.users.services;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.User;
import nl.tudelft.sem.users.UserRole;
import nl.tudelft.sem.users.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
     * Registers a new user if (s)he does not already exist and if the provided data are valid.
     * The role is initially set to STUDENT for all users.
     *
     * @param netId         the netID (username) of the user
     * @param firstName     the name of the user
     * @param lastName      the surname of the user
     * @return true is a user has been successfully registered;
     *         false if a user with the same netID already exists
     * @throws DataIntegrityViolationException if netID already exists
     *          and if any of the netID, first name and last name are blank, empty or null
     */
    public long registerUser(String netId, String firstName, String lastName)
            throws DataIntegrityViolationException {

        if (netId == null || netId.isBlank() || netId.isEmpty()) {
            throw new DataIntegrityViolationException("Please specify the netID!");
        }

        if (this.userRepository.findByUsername(netId).isPresent()) {
            String msg = String.format("User with netID %s already exist", netId);
            throw new DataIntegrityViolationException(msg);
        }

        if (firstName == null || firstName.isBlank() || firstName.isEmpty()) {
            throw new DataIntegrityViolationException("Please specify the first name!");
        }

        if (lastName == null || lastName.isBlank() || lastName.isEmpty()) {
            throw new DataIntegrityViolationException("Please specify the last name!");
        }

        User user = this.userRepository.save(new User(netId, firstName, lastName, UserRole.STUDENT));
        return user.getUserId();
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
     * Changes the role of a user to another role if the requester has permissions to do that.
     *
     * @param userId        the user ID of the user
     * @param newRole       the new role of the user
     * @param requesterRole the role of the requester
     * @return true if the operation has been successful,
     *         false if the requester does not have the required permissions
     */
    public boolean changeRole(long userId, UserRole newRole, UserRole requesterRole) {
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

        Optional<User> optionalUser = this.userRepository.findByUserId(userId);
        assert optionalUser.isPresent();
        User user = optionalUser.get();

        // Only an admin can downgrade another admin
        if (user.getRole().equals(UserRole.ADMIN) && !newRole.equals(UserRole.ADMIN)
                && !requesterRole.equals(UserRole.ADMIN)) {
            return false;
        }

        user.setRole(newRole);
        this.userRepository.save(user);
        return true;
    }


    /**
     * Deletes the user by their username (netID).
     *
     * @param userId        the user ID of the user
     * @param requesterRole the role of the requester
     * @return true if the operation has been successful,
     *         false if the requester does not have the required permissions (is not an admin)
     */
    public boolean deleteUserByUserId(long userId, UserRole requesterRole) {
        if (!requesterRole.equals(UserRole.ADMIN)) {
            return false;
        }

        this.userRepository.deleteByUserId(userId);
        return true;
    }
}
