package info.jtrac.config;

import info.jtrac.domain.User;
import info.jtrac.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class JtracUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JtracUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = userRepository.findByLoginName(username);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = users.get(0); // Assuming loginName is unique
        return new org.springframework.security.core.userdetails.User(
                user.getLoginName(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")) // Simplified roles for now
        );
    }
}
