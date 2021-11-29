package nl.tudelft.sem.authentication.repository;

import java.util.Optional;
import nl.tudelft.sem.authentication.auth.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepository extends CrudRepository<UserData, String> {
    Optional<UserData> findByUsername(String username);

//    UserData findAllByAuthorities(Set<SimpleGrantedAuthority> authorities);
}
