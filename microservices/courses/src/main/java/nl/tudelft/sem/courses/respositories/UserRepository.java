package nl.tudelft.sem.courses.respositories;


import nl.tudelft.sem.courses.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
