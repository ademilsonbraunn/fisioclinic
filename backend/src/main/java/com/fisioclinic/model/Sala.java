package com.fisioclinic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
