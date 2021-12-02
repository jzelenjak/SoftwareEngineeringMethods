package nl.tudelft.sem.authentication.repositories;

import java.util.Optional;
import nl.tudelft.sem.authentication.entities.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * The repository for storing user data.
 */
@Repository
public interface UserDataRepository extends CrudRepository<UserData, String> {

    /**
     * Finds user by their username.
     *
     * @param username the username
     * @return the user if has been found
     */
    Optional<UserData> findByUsername(String username);
}
