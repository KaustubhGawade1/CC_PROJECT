package com.dbms.databasemanagementsystem.security;

import org.springframework.beans.factory.annotation.Value;
import com.dbms.databasemanagementsystem.model.Users;
import com.dbms.databasemanagementsystem.model.acces;
import com.dbms.databasemanagementsystem.model.user_role;
import com.dbms.databasemanagementsystem.repository.UserRepository;
import com.dbms.databasemanagementsystem.repository.userRoleRepo;
import com.dbms.databasemanagementsystem.security.jwt.AuthEntryPointJwt;
import com.dbms.databasemanagementsystem.security.jwt.AuthTokenFilter;
import com.dbms.databasemanagementsystem.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://front-damss.onrender.com}")
    private String allowedOrigins;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
                    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
                    return config;
                }))



                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/asset/add/file").permitAll()
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/signin").permitAll()
                        .requestMatchers("/api/auth/signout").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Component
    public class DataInitializer {

        private final userRoleRepo roleRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(userRoleRepo roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void initData() {

            user_role userRole = roleRepository.findByUserRole(acces.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new user_role(acces.ROLE_USER)));

            user_role editorRole = roleRepository.findByUserRole(acces.ROLE_EDITOR)
                    .orElseGet(() -> roleRepository.save(new user_role(acces.ROLE_EDITOR)));

            user_role adminRole = roleRepository.findByUserRole(acces.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new user_role(acces.ROLE_ADMIN)));

            if (!userRepository.existsByusername("user1")) {
                Users user1 = new Users("user1", "user1@example.com", passwordEncoder.encode("password1"));
                user1.setRoles(Set.of(userRole));
                userRepository.save(user1);
            }

            if (!userRepository.existsByusername("Editor")) {
                Users editor = new Users("Editor", "editor@example.com", passwordEncoder.encode("password2"));
                editor.setRoles(Set.of(editorRole));
                userRepository.save(editor);
            }

            if (!userRepository.existsByusername("admin")) {
                Users admin = new Users("admin", "admin@example.com", passwordEncoder.encode("adminPass"));
                admin.setRoles(Set.of(userRole, editorRole, adminRole));
                userRepository.save(admin);
            }
        }
    }
}





