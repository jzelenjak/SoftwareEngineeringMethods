package nl.tudelft.sem.authentication.repository;

import java.util.Optional;
import nl.tudelft.sem.authentication.auth.UserData;
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
