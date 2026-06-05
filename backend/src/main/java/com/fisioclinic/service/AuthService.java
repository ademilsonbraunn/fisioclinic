package com.fisioclinic.service;

import com.fisioclinic.config.JwtUtil;
import com.fisioclinic.dto.LoginDTO;
import com.fisioclinic.dto.SenhaDTO;
import com.fisioclinic.dto.TokenResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.exception.UnauthorizedException;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.repository.FisioterapeutaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final FisioterapeutaRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration:28800000}")
    private long expiration;

    // ── Login ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TokenResponse login(LoginDTO dto) {
        Fisioterapeuta fisio = repository.findByEmail(dto.email().trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!fisio.getAtivo()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        if (fisio.getSenhaHash() == null || !passwordEncoder.matches(dto.senha(), fisio.getSenhaHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String token = jwtUtil.gerarToken(fisio);
        return new TokenResponse(token, fisio.getNome(), fisio.getPerfil().name(), expiration);
    }

    // ── Alterar senha ─────────────────────────────────────────────────────────

    public void alterarSenha(String email, SenhaDTO dto) {
        Fisioterapeuta fisio = repository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (fisio.getSenhaHash() == null || !passwordEncoder.matches(dto.senhaAtual(), fisio.getSenhaHash())) {
            throw new ConflictException("Senha atual incorreta");
        }

        fisio.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        repository.save(fisio);
    }
}
