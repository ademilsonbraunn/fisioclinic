package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Paciente — Entidade JPA que representa um paciente da clínica (Módulo 1)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: pacientes
 *
 * Chave primária: UUID gerado automaticamente pelo banco (não exposto sequencialmente).
 * CPF é único e armazenado sem máscara (apenas dígitos, 11 chars).
 * Telefone é armazenado sem máscara (apenas dígitos, 11 chars).
 *
 * Relacionamentos (entidades separadas para normalização):
 *  - ContatoEmergencia: 1 paciente → 0..1 contato de emergência
 *  - ConvenioPaciente:  1 paciente → 0..1 registro de convênio/pagamento
 *
 * Auditoria automática: createdAt e updatedAt preenchidos pelo JPA Auditing
 * (@EnableJpaAuditing na FisioclinicApplication).
 *
 * Enums centralizados aqui para garantir valores válidos em qualquer módulo:
 *  - Sexo: FEMININO | MASCULINO | OUTRO | NAO_INFORMADO
 *  - EstadoCivil: SOLTEIRO | CASADO | DIVORCIADO | VIUVO | UNIAO_ESTAVEL
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "pacientes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCivil estadoCivil;

    private String profissao;

    private String fotoUrl;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, length = 11)
    private String telefone;

    // Endereço
    @Column(length = 8)
    private String cep;

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;

    @Column(length = 2)
    private String uf;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Enums ───────────────────────────────────────────────────────────────

    public enum Sexo {
        @JsonProperty("feminino")      FEMININO,
        @JsonProperty("masculino")     MASCULINO,
        @JsonProperty("outro")         OUTRO,
        @JsonProperty("nao_informado") NAO_INFORMADO
    }

    public enum EstadoCivil {
        @JsonProperty("solteiro")      SOLTEIRO,
        @JsonProperty("casado")        CASADO,
        @JsonProperty("divorciado")    DIVORCIADO,
        @JsonProperty("viuvo")         VIUVO,
        @JsonProperty("uniao_estavel") UNIAO_ESTAVEL
    }
}
