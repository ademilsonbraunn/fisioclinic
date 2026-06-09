package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimento — TCLE e termos assinados pelo paciente (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: termos_consentimento
 *
 * Registra o momento exato em que o paciente assinou o TCLE (assinado_em).
 * Vinculado ao plano de tratamento (M3) que originou a necessidade do termo.
 *
 * Exigência legal: Resolução CFM 1.821/07 — prontuário eletrônico deve
 * registrar consentimento com autoria e data/hora.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "termos_consentimento")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class TermoConsentimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // [M1] Paciente que assinou o termo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // [M3] Plano de tratamento ao qual o termo está vinculado (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private PlanoTratamento plano;

    // Espelha o CHECK do schema: tcle | autorizacao | outro
    @Column(nullable = false, length = 20)
    private String tipo = "tcle";

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    // Timestamp da assinatura — obrigatório para validade legal
    @Column(name = "assinado_em", nullable = false)
    private LocalDateTime assinadoEm;

    @Column(name = "assinatura_url", columnDefinition = "TEXT")
    private String assinaturaUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
