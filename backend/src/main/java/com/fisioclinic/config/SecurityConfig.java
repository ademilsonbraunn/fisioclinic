package com.fisioclinic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

import java.util.Arrays;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SecurityConfig — Configuração central de segurança da aplicação
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Config / Segurança
 *
 * Modelo de autenticação: JWT stateless (sem sessão HTTP).
 * CORS: origens lidas de cors.allowed-origins em application.properties.
 *   Em produção, definir apenas o domínio real da aplicação.
 *
 * Matriz de autorização:
 *  - /api/auth/login          → público (sem token)
 *  - /api/pacientes/**        → qualquer usuário autenticado
 *  - /api/fisioterapeutas/**  → somente ADMIN
 *  - /api/salas/**            → somente ADMIN
 *  - /api/atualizacoes        → qualquer usuário autenticado
 *  - /api/anamneses/**        → qualquer usuário autenticado
 *  - demais endpoints         → qualquer usuário autenticado
 *
 * Erros padronizados: 401 Não autenticado / 403 Acesso negado em JSON.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Auth — público
                .requestMatchers("/api/auth/login").permitAll()
                // Pacientes — requer autenticação
                .requestMatchers("/api/pacientes/**").authenticated()
                // Admin — requer perfil ADMIN
                .requestMatchers("/api/fisioterapeutas/**").hasRole("ADMIN")
                .requestMatchers("/api/salas/**").hasRole("ADMIN")
                // Atualizações do sistema — qualquer usuário autenticado
                .requestMatchers("/api/atualizacoes").authenticated()
                // Anamneses — qualquer usuário autenticado
                .requestMatchers("/api/anamneses/**").authenticated()
                // Troca de senha — qualquer usuário autenticado
                .requestMatchers("/api/auth/senha").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"erro\":\"Não autenticado\",\"status\":401}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"erro\":\"Acesso negado\",\"status\":403}");
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origens = Arrays.asList(corsAllowedOrigins.split(","));
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
