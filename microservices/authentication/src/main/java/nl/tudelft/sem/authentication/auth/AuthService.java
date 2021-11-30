package nl.tudelft.sem.authentication.auth;

import nl.tudelft.sem.authentication.exceptions.UserAlreadyExistsException;
import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {
    private final transient UserDataRepository userDataRepository;
    private final transient PasswordEncoder passwordEncoder;

    public AuthService(UserDataRepository userDataRepository, PasswordEncoder passwordEncoder) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String username, String password) throws UserAlreadyExistsException {
        if (this.userDataRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        this.userDataRepository.save(new UserData(username, passwordEncoder.encode(password), UserRole.STUDENT));
    }

    @Transactional
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
