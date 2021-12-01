package nl.tudelft.sem.authentication.auth;

import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    private final transient UserDataRepository userDataRepository;
    private final transient PasswordEncoder passwordEncoder;

    public AuthService(UserDataRepository userDataRepository, PasswordEncoder passwordEncoder) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerUser(String username, String password) {
        if (this.userDataRepository.findByUsername(username).isPresent()) return false;
        this.userDataRepository.save(new UserData(username, passwordEncoder.encode(password), UserRole.STUDENT));
        return true;
    }

    public void changePassword(String username, String newPassword) {
        UserData userData = loadUserByUsername(username);
        userData.setPassword(passwordEncoder.encode(newPassword));
        userDataRepository.save(userData);
    }

    /**
     * Finds the user by their username.
     *
     * @param username  the username of the user to be found.
     *
     * @return the user, if found.
     * @throws UsernameNotFoundException thrown when user has not been found.
     */
    @Override
    public UserData loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userDataRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(
                        "User with username %s not found", username)));
    }
}
