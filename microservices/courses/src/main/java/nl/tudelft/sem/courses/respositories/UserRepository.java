package nl.tudelft.sem.courses.respositories;


import nl.tudelft.sem.courses.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
