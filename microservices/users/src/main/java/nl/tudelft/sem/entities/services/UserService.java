package nl.tudelft.sem.entities.services;

import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import nl.tudelft.sem.entities.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final transient UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(String netid, String name, String surname, UserRole role) {
        if (this.userRepository.findByUsername(netid).isPresent()) {
            return false;
        }
        this.userRepository.save(new User(netid, name, surname, role));
        return true;
    }
}
