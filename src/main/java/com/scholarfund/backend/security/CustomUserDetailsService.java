package com.scholarfund.backend.security;

import com.scholarfund.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch our custom User entity from the DB
        com.scholarfund.backend.entity.User appUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Convert it into the UserDetails object that Spring Security understands
        return org.springframework.security.core.userdetails.User.builder()
                .username(appUser.getEmail())
                // Provide dummy password if null, as Spring expects a non-null string
                .password(appUser.getPasswordHash() != null ? appUser.getPasswordHash() : "")
                .authorities("ROLE_" + appUser.getRole().getRoleName())
                .disabled(!appUser.getIsActive())
                .build();
    }
}