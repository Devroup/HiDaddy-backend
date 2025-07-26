package Devroup.hidaddy.security;

import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.user.*;
import lombok.RequiredArgsConstructor;  
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String socialId) throws UsernameNotFoundException {
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with socialId: " + socialId));
        return new UserDetailsImpl(user);
    }
}
