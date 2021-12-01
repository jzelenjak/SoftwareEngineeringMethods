package nl.tudelft.sem.users.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByRole(UserRole role);

    List<User> findAllByFirstName(String firstName);

    List<User> findAllByLastName(String lastName);
}

