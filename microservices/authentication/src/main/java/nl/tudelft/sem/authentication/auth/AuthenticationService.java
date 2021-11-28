package nl.tudelft.sem.authentication.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {
//    private Users users;
//
//    @Autowired
//    public AuthenticationService(Users users) {
//        this.users = users;
//    }
//
//    public Object loadUserByUsername(String username) throws UsernameNotFoundException {
//        return this.users.findByUsername(username).orElseThrow(() ->
//                new UsernameNotFoundException("Username: " + username + " not found")
//        );
//    }
}
