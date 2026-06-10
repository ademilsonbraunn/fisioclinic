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
 * FotoEvolucao — Fotos comparativas vinculadas a evoluções clínicas (Módulo 5)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: fotos_evolucao
 *
 * Armazena fotos before/after de cada sessão para acompanhamento visual do
 * paciente. O arquivo físico fica em uploads/evolucoes/{evolucaoId}/ no servidor;
 * a coluna 'url' guarda o caminho relativo no disco (padrão de ArquivoAnamnese).
 *
 * Tipos permitidos: antes | depois | comparativo | outro
 * (espelha o CHECK do schema — não usar enum para evitar migration ao adicionar tipos)
 *
 * [M5 P2] Fornece dados para galeria before/after exibida no card de evolução.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "fotos_evolucao")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class FotoEvolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // [M5] Vínculo com a evolução de origem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolucao_id", nullable = false)
    private Evolucao evolucao;

    // Espelha o CHECK do schema: antes | depois | comparativo | outro
    @Column(nullable = false, length = 15)
    private String tipo;

    // Nome original do arquivo para exibição
    @Column(name = "nome_arquivo", columnDefinition = "TEXT")
    private String nomeArquivo;

    // Caminho relativo no disco: {evolucaoId}/{uuid}_original.jpg
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
