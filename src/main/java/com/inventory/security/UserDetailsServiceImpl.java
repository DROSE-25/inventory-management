package com.inventory.security;
 
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.inventory.model.User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
 
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            user.getIsActive(),
            true, true, true,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

