package com.academic.service.security;

import com.academic.service.entity.AcademicUser;
import com.academic.service.repository.AcademicUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AcademicUserDetailsService implements UserDetailsService {

    private final AcademicUserRepository userRepository;

    public AcademicUserDetailsService(AcademicUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AcademicUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Academic user not found: " + username));

        return User.withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getRole())
            .build();
    }
}
