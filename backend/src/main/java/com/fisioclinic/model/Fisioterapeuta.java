package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Fisioterapeuta — Entidade JPA que representa um profissional da clínica
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: fisioterapeutas
 *
 * Dupla função: cadastro clínico (nome, CRF, telefone) e credencial de acesso
 * ao sistema (email + senhaHash + perfil). O campo senhaHash armazena o hash
 * BCrypt — nunca a senha em texto simples.
 *
 * Enum Perfil:
 *  - FISIOTERAPEUTA: acesso às telas clínicas (prontuário, agenda, evolução)
 *  - ADMIN: acesso total, incluindo cadastro de profissionais e salas
 *
 * ativo = false desativa o acesso sem excluir o histórico clínico vinculado.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "fisioterapeutas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Fisioterapeuta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 15)
    private String crf;

    @Column(unique = true)
    private String email;

    @Column(length = 11)
    private String telefone;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "senha_hash")
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil = Perfil.FISIOTERAPEUTA;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Enum ────────────────────────────────────────────────────────────────

    public enum Perfil {
        @JsonProperty("FISIOTERAPEUTA") FISIOTERAPEUTA,
        @JsonProperty("ADMIN")          ADMIN
    }
}
