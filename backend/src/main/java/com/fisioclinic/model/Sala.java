package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Sala — Entidade JPA que representa um espaço físico de atendimento
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Model (mapeamento objeto-relacional)
 * Tabela: salas
 *
 * Usada pelo Módulo 4 (Agendamento) para controle de ocupação.
 * A regra de negócio que impede dois agendamentos na mesma sala no mesmo
 * horário é verificada no SessaoService — não há constraint de BD para isso.
 *
 * Enum TipoSala:
 *  - BOX: espaço individual aberto (mais comum em clínicas de fisioterapia)
 *  - SALA_INDIVIDUAL: sala fechada para um paciente
 *  - SALA_GRUPO: espaço para atendimento de múltiplos pacientes (campo capacidade)
 *
 * ativo = false oculta a sala nos seletores sem remover histórico de sessões.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "salas")
@Getter
@Setter
@NoArgsConstructor
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSala tipo = TipoSala.BOX;

    @Column(nullable = false)
    private Integer capacidade = 1;

    @Column(nullable = false)
    private Boolean ativo = true;

    // ── Enum ────────────────────────────────────────────────────────────────

    public enum TipoSala {
        @JsonProperty("BOX")             BOX,
        @JsonProperty("SALA_INDIVIDUAL") SALA_INDIVIDUAL,
        @JsonProperty("SALA_GRUPO")      SALA_GRUPO
    }
}
