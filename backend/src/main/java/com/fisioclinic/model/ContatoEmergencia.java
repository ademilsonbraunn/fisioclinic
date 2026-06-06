package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ContatoEmergencia — Entidade JPA que representa o contato de emergência (M1)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: contatos_emergencia
 *
 * Relação 1:1 com Paciente (UniqueConstraint em paciente_id):
 * cada paciente tem no máximo um contato de emergência.
 * Todos os campos são opcionais — o contato pode ser cadastrado depois.
 *
 * Enum Parentesco define as opções válidas de grau de parentesco,
 * impedindo que o frontend envie valores livres neste campo.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "contatos_emergencia",
       uniqueConstraints = @UniqueConstraint(columnNames = "paciente_id"))
@Getter
@Setter
@NoArgsConstructor
public class ContatoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Parentesco parentesco;

    @Column(length = 11)
    private String telefone;

    // ── Enum ────────────────────────────────────────────────────────────────

    public enum Parentesco {
        @JsonProperty("conjuge")     CONJUGE,
        @JsonProperty("pai_mae")     PAI_MAE,
        @JsonProperty("filho_filha") FILHO_FILHA,
        @JsonProperty("irmao_irma")  IRMAO_IRMA,
        @JsonProperty("amigo")       AMIGO,
        @JsonProperty("outro")       OUTRO
    }
}
