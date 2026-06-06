package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Evolucao — Entidade JPA que representa o registro clínico SOAP (Módulo 5)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: evolucoes
 *
 * Registro imutável após criação (Resolução CFM 1.821/07): uma vez registrado,
 * o prontuário não deve ser alterado — a camada de serviço deve impor isso.
 *
 * Campos SOAP mapeados para colunas do schema existente:
 *  subjetivo    → coluna "subjetivo"       (S — relato do paciente)
 *  objetivo     → coluna "objetivo"        (O — achados clínicos)
 *  avaliacao    → coluna "avaliacao_clinica" (A — análise do fisioterapeuta)
 *  planoEvolucao → coluna "plano"          (P — conduta para próxima sessão)
 *
 * tecnicasRealizadas: lista JSON de técnicas efetivamente executadas
 *   (pode diferir do plano de tratamento).
 * evaAntes / evaDepois: escala EVA 0–10 para acompanhamento da dor.
 * codigoTuss: código TUSS/CBHPM para faturamento de convênio (opcional).
 *
 * Dados clínicos são sensíveis pela LGPD art. 11 — nunca logar campos TEXT.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "evolucoes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Evolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private Sessao sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id")
    private Fisioterapeuta fisioterapeuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_tratamento_id")
    private PlanoTratamento planoTratamento;

    @Column(name = "num_sessao", nullable = false)
    private Integer numSessao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "tempo_atendimento_min")
    private Integer tempoAtendimentoMin;

    // SOAP — nomes internos preservam semântica, colunas mapeadas para o schema existente
    @Column(name = "subjetivo", nullable = false, columnDefinition = "TEXT")
    private String subjetivo;

    @Column(name = "objetivo", nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @Column(name = "avaliacao_clinica", nullable = false, columnDefinition = "TEXT")
    private String avaliacao;

    @Column(name = "plano", nullable = false, columnDefinition = "TEXT")
    private String planoEvolucao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tecnicas_realizadas", columnDefinition = "jsonb")
    private List<String> tecnicasRealizadas;

    @Column(name = "aparelhos", columnDefinition = "TEXT")
    private String aparelhos;

    @Column(name = "eva_antes")
    private Integer evaAntes;

    @Column(name = "eva_depois")
    private Integer evaDepois;

    @Column(name = "codigo_tuss", length = 20)
    private String codigoTuss;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
