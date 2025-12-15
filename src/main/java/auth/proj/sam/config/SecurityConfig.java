package auth.proj.sam.config;

import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationFailureHandler customLoginFailureHandler;
    private final AuthenticationSuccessHandler customLoginSuccessHandler;

    public SecurityConfig(AuthenticationFailureHandler customLoginFailureHandler, AuthenticationSuccessHandler customLoginSuccessHandler) {
        this.customLoginFailureHandler = customLoginFailureHandler;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return new CustomUserDetails(user);
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserRepository userRepository) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService(userRepository));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ADD "/register/student" HERE
                .requestMatchers("/", "/login", "/register", "/register/student", "/verify", "/forgot-password", "/reset-password", "/mfa-verify", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/department/**").hasRole("DEPARTMENT") 
                // MODIFIED: Grant ROLE_TEACHER access to /profile/**
                .requestMatchers("/teacher/**", "/profile/**").hasAnyRole("TEACHER", "STUDENT") // Teachers and Students need /profile
                .requestMatchers("/student/**").hasRole("STUDENT") 
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customLoginSuccessHandler)
                .failureHandler(customLoginFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}