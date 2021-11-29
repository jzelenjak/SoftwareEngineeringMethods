package nl.tudelft.sem.authentication.auth;

import java.util.HashSet;
import java.util.Set;
import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final transient UserDataRepository userDataRepository;

    @Autowired
    public AuthService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    /**
     * Finds the user by their username.
     *
     * @param username  the username of the user to be found.
     *
     * @return the user, if found.
     * @throws UsernameNotFoundException thrown when user has not been found.
     */
//    @Override
    public UserData loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userDataRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User with username %s not found", username)));
    }
}
