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
                        // 1. السماح بطلبات الـ OPTIONS الخاصة بالـ CORS دائماً
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. السماح بالوصول للملفات المرفوعة والصور للجميع بدون شروط
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/api/images/**", "/images/**", "/uploads/**", "/api/vets/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/*.jpeg", "/*.jpg", "/*.png", "/*.gif").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // 3. تعديل البروفايل والصور الشخصية (مفتوح للأدوار المعتمدة والـ USER العادي احتياطاً)
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/me/avatar", "/api/auth/me/cover", "/api/auth/me/**")
                        .hasAnyAuthority("ROLE_PET_OWNER", "ROLE_VET", "ROLE_VENDOR", "USER", "ROLE_USER")

                        // 4. باثات الـ Auth الأساسية مفتوحة للكل
                        .requestMatchers("/api/auth/**", "/requester-signup", "/forgot-password").permitAll()

                        // 5. الـ AI Visual Scan مفتوح للجميع (الـ Guest والـ Authenticated) مع الـ Mapping الجديد
                        .requestMatchers("/api/ai/**").permitAll()

                        // 6. التقارير الطبية تحتاج تسجيل دخول
                        .requestMatchers("/api/pet-report/**").authenticated()

                        // 7. 🔥 حل مشكلة بيدج السوشيال والـ Community والستوري والـ Public Profiles بالكامل
                        // الـ GET مفتوح للجميع علشان البوستات وبروفايلات الناس تفتح عادي جداً بدون 403
                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/community/**", "/api/stories/**").permitAll()
                        // الـ POST والـ PUT والـ DELETE محتاجين فقط Token سليم (authenticated) للتفاعل والكتابة
                        .requestMatchers("/api/posts/**", "/api/community/**", "/api/stories/**", "/api/groups/**").authenticated()

                        // 8. 🔥 حل مشكلة صفحة الـ Friends والرسائل والإشعارات
                        // أي Request رايح للـ Friends أو الـ Messages محتاج بس يكون authenticated بدون تعقيد الـ Roles
                        .requestMatchers("/api/friends", "/api/friends/**", "/api/messages", "/api/messages/**").authenticated()

                        // 9. 🩺 باثات الدكاترة والـ Vets
                        .requestMatchers(HttpMethod.GET, "/api/vets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vets/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vets/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/vets/profile").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.GET, "/api/vets/profile").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.GET, "/api/vets/dashboard").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.PATCH, "/api/vets/{id}").hasAnyAuthority("ROLE_VET")
                        .requestMatchers(HttpMethod.POST, "/api/vets/{id}/certificate").hasAnyAuthority("ROLE_VET")
                        .requestMatchers("/api/vets/**").authenticated()

                        // 10. باقي خدمات الأبلكيشن الموثقة
                        .requestMatchers("/api/appointments/**", "/api/notifications/**", "/api/pets/**", "/api/medical/**").authenticated()

                        // 11. باثات الإدارة والـ Admin
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // أي Request آخر غير محدد فوق يحتاج تسجيل دخول
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}