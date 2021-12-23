package nl.tudelft.sem.users.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A class for a user repository.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    /**
     * Finds a user by the username.
     *
     * @param username the username
     * @return the user who has the given username
     */
    Optional<User> findByUsername(String username);


    /**
     * Finds a user by the user id.
     *
     * @param userId the user id
     * @return the user who has the given user id
     */
    Optional<User> findByUserId(long userId);

    /**
     * Finds all users having the given role.
     *
     * @param role the role of a user
     * @return the list of users who have the given role
     */
    List<User> findAllByRole(UserRole role);

    /**
     * Deletes the user with the given user ID.
     *
     * @param userId the user ID
     */
    void deleteByUserId(long userId);
}

