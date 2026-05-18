package com.smartbanking.config;

import com.smartbanking.security.CustomUserDetailsService;
import com.smartbanking.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
     * JwtAuthenticationFilter प्रत्येक request मधून JWT token check करतो.
     * म्हणजे request authenticated आहे का नाही ते इथे verify होतं.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
     * CustomUserDetailsService database मधून user email वरून user load करतो.
     * Login time Spring Security याच service चा use करते.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /*
     * Main security configuration.
     * कोणता API public आहे, कोणता protected आहे,
     * कोणता admin only आहे हे इथे define करतो.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /*
                 * CSRF disable:
                 * आपण REST API + JWT वापरतोय.
                 * Session based login वापरत नाही, म्हणून CSRF disable ठेवतो.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /*
                 * CORS enable:
                 * Later Angular frontend localhost:4200 वरून backend call करेल.
                 */
                .cors(Customizer.withDefaults())

                /*
                 * Stateless session:
                 * JWT मध्ये server session maintain करत नाही.
                 * प्रत्येक request token ने verify होते.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /*
                 * API access rules
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public APIs:
                         * हे APIs token शिवाय access होतील.
                         */
                        .requestMatchers(
                                "/api/health",
                                "/api/health/**",
                                "/api/users/register",
                                "/api/users/login",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * Account APIs:
                         * Login केलेला USER किंवा ADMIN access करू शकतो.
                         */
                        .requestMatchers("/api/accounts/**").hasAnyRole("USER", "ADMIN")

                        /*
                         * Admin APIs:
                         * फक्त ADMIN role access करू शकतो.
                         */
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        /*
                         * बाकी सर्व APIs login required.
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Authentication provider:
                 * Email/password verify करण्यासाठी customUserDetailsService + BCrypt use होतो.
                 */
                .authenticationProvider(authenticationProvider())

                /*
                 * JWT filter add:
                 * Spring Security च्या default UsernamePasswordAuthenticationFilter आधी
                 * आपला JWT filter run होईल.
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                /*
                 * Basic Auth आणि Form Login disable:
                 * कारण आपण JWT based API authentication वापरत आहोत.
                 */
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /*
     * AuthenticationProvider:
     * हा login time email/password verify करतो.
     *
     * Flow:
     * 1. User email देतो
     * 2. CustomUserDetailsService database मधून user शोधतो
     * 3. PasswordEncoder BCrypt password compare करतो
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /*
     * AuthenticationManager:
     * Login API मध्ये AuthServiceImpl मधून आपण हे use करतो.
     *
     * Example:
     * authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(email, password)
     * );
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    /*
     * PasswordEncoder:
     * Plain password database मध्ये save करू नये.
     * BCrypt password hash बनवतो.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /*
     * CORS Configuration:
     * Angular frontend मधून backend API call करण्यासाठी.
     * Later frontend URL: http://localhost:4200
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}