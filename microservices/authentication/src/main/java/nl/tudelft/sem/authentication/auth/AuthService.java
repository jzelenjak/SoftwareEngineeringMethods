package nl.tudelft.sem.authentication.auth;

import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {
    private transient final UserDataRepository userDataRepository;

    @Autowired
    public AuthService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public UserData findUserByNetID(String netID) throws UsernameNotFoundException {
        return this.userDataRepository
                .findByUsername(netID)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with NetID %s not found", netID)));
    }

    public boolean registerUser(String netID, String password, String role) {
        // TODO: Change it
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(role));

        this.userDataRepository.save(new UserData(netID, password, UserRole.STUDENT));
        return true;
    }
}
