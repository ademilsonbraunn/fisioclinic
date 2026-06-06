package com.fisioclinic.config;

import com.fisioclinic.repository.FisioterapeutaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * JwtFilter — Filtro de autenticação JWT executado uma vez por requisição
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Config / Segurança
 *
 * Fluxo por requisição:
 *  1. Extrai o token do header "Authorization: Bearer <token>"
 *  2. Valida assinatura e expiração via JwtUtil
 *  3. Verifica se o fisioterapeuta ainda está ativo no banco
 *  4. Se tudo ok, registra a autenticação no SecurityContext com a role
 *     "ROLE_<perfil>" (ex: ROLE_ADMIN, ROLE_FISIOTERAPEUTA)
 *
 * A consulta ao banco no passo 3 impede que tokens válidos de profissionais
 * desativados continuem autenticando — custo: 1 SELECT por requisição.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final FisioterapeutaRepository fisioterapeutaRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isValido(token)) {
                String email  = jwtUtil.extrairEmail(token);
                String perfil = jwtUtil.extrairPerfil(token);

                boolean ativo = fisioterapeutaRepository
                    .findByEmail(email)
                    .map(f -> f.getAtivo())
                    .orElse(false);

                if (ativo) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + perfil);
                    var auth = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
