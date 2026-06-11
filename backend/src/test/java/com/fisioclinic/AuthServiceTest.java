package com.fisioclinic;

import com.fisioclinic.config.JwtUtil;
import com.fisioclinic.dto.LoginDTO;
import com.fisioclinic.dto.SenhaDTO;
import com.fisioclinic.dto.TokenResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.UnauthorizedException;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthService — autenticação e alteração de senha.
 * Usa Mockito para evitar dependência de banco de dados em tempo de teste.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FisioterapeutaRepository repository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private PasswordEncoder encoder;
    private Fisioterapeuta fisioAtivo;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(authService, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(authService, "expiration", 28800000L);

        fisioAtivo = new Fisioterapeuta();
        fisioAtivo.setId(UUID.randomUUID());
        fisioAtivo.setEmail("joao@clinica.com");
        fisioAtivo.setNome("João Silva");
        fisioAtivo.setSenhaHash(encoder.encode("Senha@123"));
        fisioAtivo.setPerfil(Fisioterapeuta.Perfil.FISIOTERAPEUTA);
        fisioAtivo.setAtivo(true);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login com credenciais válidas retorna token")
    void loginValido_retornaToken() {
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));
        when(jwtUtil.gerarToken(fisioAtivo)).thenReturn("token-jwt");

        TokenResponse resp = authService.login(new LoginDTO("joao@clinica.com", "Senha@123"));

        assertThat(resp.token()).isEqualTo("token-jwt");
        assertThat(resp.nome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Login com email inexistente lança UnauthorizedException")
    void loginEmailInexistente_lancaUnauthorized() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginDTO("x@x.com", "senha")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Login com senha incorreta lança UnauthorizedException")
    void loginSenhaErrada_lancaUnauthorized() {
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));

        assertThatThrownBy(() -> authService.login(new LoginDTO("joao@clinica.com", "senhaErrada")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Login com usuário inativo lança UnauthorizedException")
    void loginUsuarioInativo_lancaUnauthorized() {
        fisioAtivo.setAtivo(false);
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));

        assertThatThrownBy(() -> authService.login(new LoginDTO("joao@clinica.com", "Senha@123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Usuário inativo");
    }

    @Test
    @DisplayName("Login normaliza email para lowercase antes de buscar")
    void login_normalizaEmailLowercase() {
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));
        when(jwtUtil.gerarToken(fisioAtivo)).thenReturn("token");

        authService.login(new LoginDTO("JOAO@CLINICA.COM", "Senha@123"));

        verify(repository).findByEmail("joao@clinica.com");
    }

    // ── Alterar senha ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Alterar senha com senha atual correta persiste nova senha")
    void alterarSenhaValida_salvaNovaSenha() {
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));

        authService.alterarSenha("joao@clinica.com",
            new SenhaDTO("Senha@123", "NovaSenha@456"));

        verify(repository).save(argThat(f ->
            encoder.matches("NovaSenha@456", f.getSenhaHash())
        ));
    }

    @Test
    @DisplayName("Alterar senha com senha atual incorreta lança ConflictException")
    void alterarSenhaSenhaErrada_lancaConflict() {
        when(repository.findByEmail("joao@clinica.com")).thenReturn(Optional.of(fisioAtivo));

        assertThatThrownBy(() ->
            authService.alterarSenha("joao@clinica.com",
                new SenhaDTO("senhaErrada", "NovaSenha@456"))
        ).isInstanceOf(ConflictException.class);
    }
}
