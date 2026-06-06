package com.fisioclinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AtualizacaoSistema — Entidade JPA para o card de novidades do sistema
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: atualizacoes_sistema
 *
 * Exibida no dashboard como "O que há de novo" para informar os usuários
 * sobre funcionalidades lançadas. O conteúdo deve ser escrito em linguagem
 * acessível — sem jargão técnico (ver CLAUDE.md, seção Card de Atualizações).
 *
 * tipo: NOVO_RECURSO | MELHORIA | CORRECAO (string — validado no Service).
 * ativo = false oculta o card sem excluir o registro histórico.
 * versao: número da versão do sistema na data do lançamento (ex: "v1.2").
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "atualizacoes_sistema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class AtualizacaoSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 10)
    private String versao;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
