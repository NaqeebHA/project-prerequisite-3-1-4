package task_3.task_3_1_4.service;

import bootstrap_demo.model.User;
import bootstrap_demo.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (User) userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found for Username: " + username));

    }
}
