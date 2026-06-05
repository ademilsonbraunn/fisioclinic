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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "anamneses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fisioterapeuta_id")
    private Fisioterapeuta fisioterapeuta;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao = LocalDate.now();

    @Column(name = "queixa_principal", nullable = false, columnDefinition = "TEXT")
    private String queixaPrincipal;

    @Column(name = "historico_doenca_atual", nullable = false, columnDefinition = "TEXT")
    private String historicoDoencaAtual;

    @Column(name = "tempo_inicio_sintomas", columnDefinition = "TEXT")
    private String tempoInicioSintomas;

    @Column(name = "doencas_preexistentes", columnDefinition = "TEXT")
    private String doencasPreexistentes;

    @Column(name = "cirurgias_anteriores", columnDefinition = "TEXT")
    private String cirurgiasAnteriores;

    @Column(columnDefinition = "TEXT")
    private String medicamentos;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "historico_familiar", columnDefinition = "TEXT")
    private String historicoFamiliar;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "avaliacao_fisica", columnDefinition = "jsonb")
    private Map<String, Object> avaliacaoFisica;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
