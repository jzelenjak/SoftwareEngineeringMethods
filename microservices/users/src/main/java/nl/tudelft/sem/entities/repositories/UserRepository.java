package nl.tudelft.sem.entities.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A class for a user repository.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    /**
     * Finds a user by the username (netId).
     *
     * @param username the username (netId)
     * @return the user who has the given username (netId)
     */
    Optional<User> findByUsername(String username);


    /**
     * Finds a user by the user id.
     *
     * @param userId the user id (netId)
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
}

