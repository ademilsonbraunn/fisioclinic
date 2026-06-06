package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Alta — Entidade JPA que representa o encerramento do tratamento (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: altas
 *
 * Evento terminal do ciclo clínico — consome dados de todos os módulos:
 *  paciente         → M1 (chave central do prontuário)
 *  planoTratamento  → M3 (plano que motivou o tratamento; ao registrar alta,
 *                         o serviço marca o plano como "concluido")
 *  fisioterapeuta   → M4 (profissional responsável pela alta)
 *  numSessoesRealizadas → calculado automaticamente pelo AltaService
 *                         com base nas sessões com status REALIZADO (M4/M5)
 *
 * motivo: domínio controlado por CHECK no banco:
 *   alta_clinica | alta_administrativa | desistencia | encaminhamento | obito
 *
 * satisfacaoNota: pesquisa de satisfação do paciente (1–5), opcional.
 *
 * Dados clínicos são sensíveis pela LGPD art. 11 — nunca logar campos TEXT.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "altas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Alta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // [M1] Paciente encaminhado para alta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // [M3] Plano de tratamento encerrado por esta alta (opcional — pode haver alta sem plano formal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private PlanoTratamento planoTratamento;

    // [M4] Fisioterapeuta responsável pela alta (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id")
    private Fisioterapeuta fisioterapeuta;

    @Column(name = "data_alta", nullable = false)
    private LocalDate dataAlta;

    // Domínio: alta_clinica | alta_administrativa | desistencia | encaminhamento | obito
    @Column(name = "motivo", nullable = false, length = 25)
    private String motivo;

    @Column(name = "resultado_objetivos", nullable = false, columnDefinition = "TEXT")
    private String resultadoObjetivos;

    @Column(name = "orientacoes_domiciliares", columnDefinition = "TEXT")
    private String orientacoesDomiciliares;

    // Relatório narrativo da evolução — para o prontuário permanente
    @Column(name = "relatorio_evolucao", columnDefinition = "TEXT")
    private String relatorioEvolucao;

    // Relatório destinado ao médico solicitante
    @Column(name = "relatorio_medico", columnDefinition = "TEXT")
    private String relatorioMedico;

    // [M4/M5] Contagem de sessões com status REALIZADO — calculado pelo AltaService
    @Column(name = "num_sessoes_realizadas")
    private Integer numSessoesRealizadas;

    @Column(name = "agendamento_retorno")
    private LocalDate agendamentoRetorno;

    // Pesquisa de satisfação opcional — nota 1 a 5
    @Column(name = "satisfacao_nota")
    private Integer satisfacaoNota;

    @Column(name = "satisfacao_comentario", columnDefinition = "TEXT")
    private String satisfacaoComentario;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
