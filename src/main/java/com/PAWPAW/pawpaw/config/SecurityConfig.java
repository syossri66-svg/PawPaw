package com.PAWPAW.pawpaw.config;

import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://pawpaw-app-cb-ay9nv29jd8tt8etcicz8v2.streamlit.app",
                "https://pawpaw-app.up.railway.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/uploads/**", "uploads/**", "/api/images/**", "/images/**", "/api/vets/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/**.jpeg", "/**.jpg", "/**.png", "/**.gif").permitAll()

                        .requestMatchers(HttpMethod.PATCH, "/api/auth/me/avatar", "/api/auth/me/cover", "/api/auth/me/**")
                        .hasAnyAuthority("ROLE_PET_OWNER", "ROLE_VET", "ROLE_VENDOR", "USER", "ROLE_USER")

                        .requestMatchers("/api/auth/**", "/requester-signup", "/forgot-password").permitAll()

                        // تأمين شات الـ AI بحيث يحتاج Token سليم
                        .requestMatchers("/api/ai/chats/**").authenticated()
                        .requestMatchers("/api/ai/**").permitAll()

                        .requestMatchers("/api/pet-report/**").authenticated()

                        // 🔥 تعديلات منة المضمونة للـ Profiles والـ User Posts
                        .requestMatchers(HttpMethod.GET, "/api/community/profiles/lookup").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/community/profiles/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/posts/user/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/community/**", "/api/stories/**").permitAll()
                        .requestMatchers("/api/posts/**", "/api/community/**", "/api/stories/**", "/api/groups/**").authenticated()

                        .requestMatchers("/api/friends", "/api/friends/**", "/api/messages", "/api/messages/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/vets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vets/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vets/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/vets/profile").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.GET, "/api/vets/profile").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.GET, "/api/vets/dashboard").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.PATCH, "/api/vets/{id}").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.POST, "/api/vets/{id}/certificate").hasAnyAuthority("ROLE_VET")
                        .requestMatchers("/api/vets/**").authenticated()

                        .requestMatchers("/api/appointments/**", "/api/notifications/**", "/api/pets/**", "/api/medical/**").authenticated()
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}