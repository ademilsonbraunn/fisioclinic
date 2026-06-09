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
 * ArquivoAnamnese — Arquivos anexados à anamnese (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: arquivos_anamnese
 *
 * Armazena metadados de exames, laudos e encaminhamentos vinculados a uma
 * avaliação. O arquivo físico fica em uploads/anamneses/{anamneseId}/ no
 * servidor; a coluna 'url' guarda o caminho relativo no disco.
 *
 * Tipos permitidos: exame | laudo | encaminhamento | outro
 * (espelha o CHECK do schema.sql — não usar enum para evitar migration ao
 *  adicionar tipos futuros)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "arquivos_anamnese")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class ArquivoAnamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // [M2] Vínculo com a anamnese de origem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id", nullable = false)
    private Anamnese anamnese;

    // Espelha o CHECK do schema: exame | laudo | encaminhamento | outro
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "nome_arquivo", nullable = false, columnDefinition = "TEXT")
    private String nomeArquivo;

    // Caminho relativo no disco: {anamneseId}/{uuid}_original.pdf
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
