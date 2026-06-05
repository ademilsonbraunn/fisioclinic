package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
