package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
