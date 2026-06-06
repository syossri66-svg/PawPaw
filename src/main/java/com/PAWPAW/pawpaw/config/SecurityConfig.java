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
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // السماح بكل الـ OPTIONS ميثود (ضروري للـ CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // الروابط العامة
                        .requestMatchers("/api/auth/**", "/requester-signup", "/forgot-password").permitAll()
                        .requestMatchers("/uploads/**", "/api/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/upload").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/stats").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()

                        // الـ Community والـ Posts (السماح بالـ GET للجميع)
                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/community/**").permitAll()

                        // الروابط المحمية
                        .requestMatchers("/api/posts/**", "/api/community/**", "/api/groups/**").authenticated()
                        .requestMatchers("/api/friends/**", "/api/messages/**").authenticated()
                        .requestMatchers("/api/vets/**").authenticated()
                        .requestMatchers("/api/appointments/**", "/api/notifications/**", "/api/pets/**", "/api/medical/**").authenticated()
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // القائمة المسموح لها بالوصول
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
}