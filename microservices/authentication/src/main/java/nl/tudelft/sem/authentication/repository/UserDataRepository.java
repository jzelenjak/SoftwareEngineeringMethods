package nl.tudelft.sem.authentication.repository;

import nl.tudelft.sem.authentication.auth.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserDataRepository extends CrudRepository<UserData, String> {
    Optional<UserData> findByUsername(String username);

    UserData findAllByAuthorities(Set<SimpleGrantedAuthority> authorities);
}
