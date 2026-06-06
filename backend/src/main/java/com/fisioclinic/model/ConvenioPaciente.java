package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ConvenioPaciente — Entidade JPA que representa o vínculo financeiro (Módulo 1)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: convenios_paciente
 *
 * Registra se o paciente é particular ou conveniado e os dados do plano.
 * tipoPagamento é obrigatório; nomeConvenio, numCarteirinha, validadePlano e
 * responsavelFinanceiro são relevantes apenas quando tipo = CONVENIO.
 *
 * validadePlano armazenado no formato "YYYY-MM" (ex: "2026-12") para
 * facilitar comparações de validade sem precisar de dia exato.
 *
 * Enum TipoPagamento:
 *  - PARTICULAR: sem vínculo com plano de saúde
 *  - CONVENIO:   atendimento coberto (total ou parcialmente) por plano
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "convenios_paciente")
@Getter
@Setter
@NoArgsConstructor
public class ConvenioPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoPagamento tipoPagamento;

    private String nomeConvenio;
    private String numCarteirinha;

    /** Formato: "YYYY-MM" */
    @Column(length = 7)
    private String validadePlano;

    private String responsavelFinanceiro;

    // ── Enum ────────────────────────────────────────────────────────────────

    public enum TipoPagamento {
        @JsonProperty("particular") PARTICULAR,
        @JsonProperty("convenio")   CONVENIO
    }
}
