package nl.tudelft.sem.users.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final transient Set<UserRole> rolesAllowedToChange;
    private final transient Set<UserRole> rolesForLecturersToChange;

    /**
     * Instantiates a new UserService object.
     *
     * @param userRepository the user repository
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.rolesAllowedToChange = Set.of(UserRole.LECTURER, UserRole.ADMIN);
        this.rolesForLecturersToChange = Set.of(UserRole.STUDENT, UserRole.TA);
    }


    /**
     * Registers a new user if (s)he does not already exist and if the provided data are valid.
     *      The role is initially set to STUDENT for all users.
     * Regarding TA's feedback on many if statements: unfortunately since Java doesn't allow
     *   switch statements on multiple variables, I could not fully incorporate the feedback.
     *   However, I tried to refactor the method a bit, and I think this is more clean.
     *
     * @param netId         the netID (username) of the user
     * @param firstName     the name of the user
     * @param lastName      the surname of the user
     * @return true is a user has been successfully registered;
     *         false if a user with the same netID already exists
     * @throws DataIntegrityViolationException if netID already exists
     *          and if any of the netID, first name and last name are blank or null
     */
    public long registerUser(String netId, String firstName, String lastName)
            throws DataIntegrityViolationException {

        if (nullOrBlank(netId)) {
            throw new DataIntegrityViolationException("Please specify the netID!");
        }

        if (this.userRepository.findByUsername(netId).isPresent()) {
            String msg = String.format("User with netID %s already exist", netId);
            throw new DataIntegrityViolationException(msg);
        }

        if (nullOrBlank(firstName) || nullOrBlank(lastName)) {
            throw new DataIntegrityViolationException("Please specify the first and last name!");
        }

        return this.userRepository
            .save(new User(netId, firstName, lastName, UserRole.STUDENT)).getUserId();
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
        if (!isAllowedToChangeRole(userId, newRole, requesterRole)) {
            return false;
        }

        // The presence of the user has already been checked in isAllowedToChangeRole method
        User user = this.userRepository.findByUserId(userId).get();

        user.setRole(newRole);
        this.userRepository.save(user);
        return true;
    }

    /**
     * A helper method that checks if the requester is allowed to change
     *      the role of a user with user ID userId.
     * Regarding TA's feedback on many if statements: unfortunately since Java doesn't allow
     *   switch statements on multiple variables, I could not fully incorporate the feedback.
     *   However, I tried to refactor the method a bit, and I think this is more clean.
     *
     * @param userId            the user ID of the user
     * @param newRole           the new role of the user
     * @param requesterRole     the role of the requester
     * @return whether the requester has enough permissions to change the role of the user
     */
    public boolean isAllowedToChangeRole(long userId, UserRole newRole, UserRole requesterRole) {
        // Only admins and lecturers can change permissions
        if (!this.rolesAllowedToChange.contains(requesterRole)) {
            return false;
        }

        Optional<User> user = this.userRepository.findByUserId(userId);
        if (user.isEmpty()) {
            return false;
        }

        // Lecturer can only change a student's role to TA or a TA's role to student
        if (requesterRole.equals(UserRole.LECTURER)) {
            return (rolesForLecturersToChange.contains(user.get().getRole())
                        && rolesForLecturersToChange.contains(newRole));
        }
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
        if (!isAllowedToDelete(requesterRole)) {
            return false;
        }

        this.userRepository.deleteByUserId(userId);
        return true;
    }

    /**
     * A helper method that checks if the requester is allowed to delete a user.
     *
     * @param requesterRole the role of the requester
     * @return whether the requester has enough permissions to delete a user.
     */
    public boolean isAllowedToDelete(UserRole requesterRole) {
        return requesterRole.equals(UserRole.ADMIN);
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
