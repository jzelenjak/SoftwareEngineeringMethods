package nl.tudelft.sem.entities.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByRole(UserRole role);

    List<User> findAllByFirstNameAndLastName(String firstName, String lastname);
}

