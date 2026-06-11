package com.fisioclinic.model;

import com.fisioclinic.model.enums.StatusPlanoTratamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PlanoTratamento — Entidade JPA que representa o plano terapêutico (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: planos_tratamento
 *
 * Gerado a partir da Anamnese (M2) e consumido pelo Agendamento (M4): os
 * campos frequenciaSemanal e numSessoesEstimado orientam a criação das sessões.
 *
 * diagnosticoCif: diagnóstico segundo a Classificação Internacional de
 *   Funcionalidade (CIF) — campo obrigatório pelo CFM.
 * cid10: opcional, código CID-10 para faturamento de convênio.
 * tecnicas: lista JSON de técnicas e recursos fisioterapêuticos planejados.
 * status: ATIVO | CONCLUIDO | CANCELADO — enum StatusPlanoTratamento (item #19 resolvido).
 *   O StatusPlanoTratamentoConverter @Converter(autoApply=true) persiste como lowercase.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "planos_tratamento")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class PlanoTratamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id")
    private Anamnese anamnese;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id")
    private Fisioterapeuta fisioterapeuta;

    @Column(name = "diagnostico_cif", nullable = false, columnDefinition = "TEXT")
    private String diagnosticoCif;

    @Column(name = "cid10", length = 10)
    private String cid10;

    @Column(name = "objetivos_curto_prazo", nullable = false, columnDefinition = "TEXT")
    private String objetivosCurtoPrazo;

    @Column(name = "objetivos_longo_prazo", nullable = false, columnDefinition = "TEXT")
    private String objetivosLongoPrazo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tecnicas", columnDefinition = "jsonb")
    private List<String> tecnicas;

    @Column(name = "frequencia_semanal", nullable = false)
    private Integer frequenciaSemanal;

    @Column(name = "num_sessoes_estimado", nullable = false)
    private Integer numSessoesEstimado;

    @Column(name = "hipoteses_tratamento", columnDefinition = "TEXT")
    private String hipotesesTratamento;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio = LocalDate.now();

    @Column(name = "data_previsao_alta")
    private LocalDate dataPrevisaoAlta;

    // Converter @Converter(autoApply=true) cuida da persistência como lowercase — sem @Enumerated nem length
    @Column(name = "status", nullable = false)
    private StatusPlanoTratamento status = StatusPlanoTratamento.ATIVO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
