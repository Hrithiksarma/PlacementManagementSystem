package com.pmrs.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter,
                          JwtAuthenticationEntryPoint entryPoint,
                          CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .authorizeHttpRequests(auth -> auth

                // ── Public ────────────────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/dashboard/**").permitAll()
                .requestMatchers(
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()

                // ── Students: read → both roles; mutate → ADMIN only ──────
                .requestMatchers(HttpMethod.GET,    "/students/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.POST,   "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/students/**").hasRole("ADMIN")

                // ── Companies: delete → ADMIN only; rest → both roles ─────
                .requestMatchers(HttpMethod.DELETE, "/companies/**").hasRole("ADMIN")
                .requestMatchers("/companies/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Drives: delete of completed → checked at controller; rest → both ──
                .requestMatchers("/drives/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Google Form drive submissions: review + include → both roles ──
                .requestMatchers("/form-submissions/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Google Form student registrations: review + import → both roles ──
                .requestMatchers("/student-form-submissions/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Withdrawal/decline penalties: review + lift → both roles ──
                .requestMatchers("/penalties/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Applications: status-email action allowed for staff…
                .requestMatchers(HttpMethod.POST, "/applications/*/send-status-email")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                // …but application creation stays blocked (only via /api/student/apply)
                .requestMatchers(HttpMethod.POST, "/applications/**").denyAll()
                // Applications: everything else → ADMIN or PLACEMENT_OFFICER
                .requestMatchers("/applications/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Student portal: STUDENT role only ─────────────────────
                .requestMatchers("/api/student/**").hasRole("STUDENT")

                // ── Admin-only: officer account management, never PLACEMENT_OFFICER ──
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── External (non-PRMS) HR contacts: same staff access as HR contacts ──
                .requestMatchers("/external-hrcontacts/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Supporting data ────────────────────────────────────────
                .requestMatchers("/departments/**", "/hrcontacts/**",
                                 "/eligibilitycriteria/**")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // Everything else must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
