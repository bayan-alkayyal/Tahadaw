package org.example.tahadaw.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // ---------- Public (no auth) ----------
                        // Registration.
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                        // Invitee group-gift voting pages.
                        .requestMatchers("/api/v1/public/**").permitAll()
                        // Moyasar server-to-server webhook + the browser callback verification
                        // used by the standalone payment-success page (no session/credentials).
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook/moyasar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/moyasar-status/**").permitAll()

                        // ---------- Admin only ----------
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/get").hasAuthority("ADMIN")
                        // Required-question catalog management (the per-plan list stays open to users below).
                        .requestMatchers(
                                "/api/v1/required-questions/add",
                                "/api/v1/required-questions/update/**",
                                "/api/v1/required-questions/delete/**",
                                "/api/v1/required-questions/disable/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/required-questions/get").hasAuthority("ADMIN")
                        // AI-question CRUD admin only
                        .requestMatchers(HttpMethod.POST, "/api/v1/ai-questions/create/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ai-questions/get", "/api/v1/ai-questions/get-by-id/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ai-questions/update/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ai-questions/delete/**").hasAuthority("ADMIN")
                        // AI-answer CRUD admin only
                        .requestMatchers(HttpMethod.POST, "/api/v1/ai-answers/ai-question/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ai-answers/get", "/api/v1/ai-answers/get-by-id/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ai-answers/update/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ai-answers/delete/**").hasAuthority("ADMIN")
                        // required question-answer CRUD
                        .requestMatchers(HttpMethod.POST, "/api/v1/required-questions-answer/required-question/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/required-questions-answer/get", "/api/v1/required-questions-answer/get-by-id/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/required-questions-answer/update/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/required-questions-answer/delete/**").hasAuthority("ADMIN")

                        // everything else needs a logged-in user
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true))
                .httpBasic(httpBasic -> {});
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Allows the standalone payment-result page (GitHub Pages) and local dev to call the API
     * from the browser. This is additive and does not change how the backend runs on AWS.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "https://saudshafie.github.io",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
