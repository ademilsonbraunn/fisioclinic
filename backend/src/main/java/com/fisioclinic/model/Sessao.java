package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Sessao — Entidade JPA que representa um agendamento de atendimento (Módulo 4)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: sessoes
 *
 * Elo central do fluxo clínico: vincula paciente + fisioterapeuta + sala em
 * um slot de tempo. O sessao.id é a chave estrangeira consumida pelo Módulo 5
 * (Evolucao) — cada evolução pertence a exatamente uma sessão.
 *
 * Enums centralizados aqui (reutilizados em SessaoService, DTOs e frontend):
 *  TipoSessao:   AVALIACAO | SESSAO | REAVALIACAO | ALTA
 *  StatusSessao: AGENDADO | CONFIRMADO | REALIZADO | FALTOU | CANCELADO
 *
 * motivoCancelamento é preenchido apenas quando status = CANCELADO.
 * dataHoraFim e duracaoMinutos são opcionais — calculados pela recepção ou
 * ao registrar a evolução.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "sessoes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id", nullable = false)
    private Fisioterapeuta fisioterapeuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Column(name = "duracao_minutos")
    private Integer duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sessao", nullable = false, length = 20)
    private TipoSessao tipoSessao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusSessao status;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "observacoes")
    private String observacoes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum TipoSessao {
        AVALIACAO, SESSAO, REAVALIACAO, ALTA
    }

    public enum StatusSessao {
        AGENDADO, CONFIRMADO, REALIZADO, FALTOU, CANCELADO
    }
}
