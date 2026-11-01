
package com.example.bookstore.config;

import com.example.bookstore.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            Environment environment,
            @Value("${app.cors.allowed-origins:http://localhost:3000}")
            String allowedOrigins) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;

        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        boolean devProfile =
                environment.acceptsProfiles(Profiles.of("dev"));

        http
                // JWT-based stateless authentication
                .csrf(csrf -> csrf.disable())

                // CORS configuration
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                // No HTTP session required
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> {

                    // Allow CORS preflight requests
                    auth.requestMatchers(
                            HttpMethod.OPTIONS, "/**"
                    ).permitAll();

                    // Public endpoints
                    auth.requestMatchers(
                            "/api/auth/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/error"
                    ).permitAll();

                    // H2 console only in development
                    if (devProfile) {
                        auth.requestMatchers(
                                "/h2-console/**"
                        ).permitAll();
                    }

                    // Public book browsing
                    auth.requestMatchers(
                            HttpMethod.GET,
                            "/api/books",
                            "/api/books/**"
                    ).permitAll();

                    // All remaining endpoints require authentication
                    auth.anyRequest().authenticated();
                })

                // H2 console requires same-origin frames
                .headers(headers -> {
                    if (devProfile) {
                        headers.frameOptions(frame ->
                                frame.sameOrigin()
                        );
                    }
                })

                // Authentication and authorization error handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                        .accessDeniedHandler((request, response, ex) ->
                                response.sendError(
                                        HttpStatus.FORBIDDEN.value()
                                )
                        )
                )

                // Register JWT filter before username/password filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}
