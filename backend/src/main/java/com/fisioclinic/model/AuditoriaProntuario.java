package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AuditoriaProntuario — Trilha de eventos do prontuário (Auditoria — P2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: auditoria_prontuario
 *
 * Registra toda criação/alteração/assinatura em entidades clínicas para
 * conformidade com a Resolução CFM 1.821/07 (imutabilidade e rastreabilidade
 * de prontuários eletrônicos).
 *
 * Entidade IMUTÁVEL — apenas INSERT, nunca UPDATE nem DELETE.
 * Por isso não tem setters nem @LastModifiedDate.
 *
 * [M2/M3/M5/M6] Alimentada por AnamneseService, PlanoTratamentoService,
 * EvolucaoService e AltaService.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "auditoria_prontuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class AuditoriaProntuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // [M1] Paciente cujo prontuário foi alterado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Espelha o CHECK do schema: ANAMNESE | EVOLUCAO | PLANO | ALTA
    @Column(name = "tipo_entidade", nullable = false, length = 20)
    private String tipoEntidade;

    // ID da entidade que foi criada/alterada
    @Column(name = "entidade_id", nullable = false)
    private UUID entidadeId;

    // Espelha o CHECK do schema: CRIACAO | ALTERACAO | ASSINATURA
    @Column(nullable = false, length = 20)
    private String acao;

    // [M2/M3/M5/M6] Fisioterapeuta responsável pela ação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id")
    private Fisioterapeuta fisioterapeuta;

    // Snapshot de dados relevantes no momento do evento (LGPD: apenas metadados, não dados clínicos sensíveis)
    // CORREÇÃO: @JdbcTypeCode(JSON) necessário no Hibernate 6 para mapear String → JSONB corretamente
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_novos", columnDefinition = "jsonb")
    private String dadosNovos;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Construtor de conveniência — entidade imutável, sem setters públicos
    public AuditoriaProntuario(Paciente paciente, String tipoEntidade, UUID entidadeId,
                                String acao, Fisioterapeuta fisioterapeuta, String dadosNovos) {
        this.paciente      = paciente;
        this.tipoEntidade  = tipoEntidade;
        this.entidadeId    = entidadeId;
        this.acao          = acao;
        this.fisioterapeuta = fisioterapeuta;
        this.dadosNovos    = dadosNovos;
    }
}
