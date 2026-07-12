package com.pmrs.backend.security;

import com.pmrs.backend.entity.User;
import com.pmrs.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService,
                      UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                String username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // These two checks apply to every authenticated request except
                    // /auth/** — otherwise a disabled/unreset account could never
                    // reach the very endpoints that fix its own state.
                    if (!request.getRequestURI().startsWith("/auth/")) {
                        User user = userRepository.findByUsername(username).orElse(null);

                        if (user != null && !user.isEnabled()) {
                            // Mid-session revocation: a still-valid JWT for a
                            // disabled account must stop working immediately,
                            // not just be blocked from future logins.
                            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                "ACCOUNT_DISABLED", "This account has been disabled.");
                            return;
                        }
                        if (user != null && user.isMustChangePassword()) {
                            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                "PASSWORD_CHANGE_REQUIRED", "You must change your password before continuing.");
                            return;
                        }
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
