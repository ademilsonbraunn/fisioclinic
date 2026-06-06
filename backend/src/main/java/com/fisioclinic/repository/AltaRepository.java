package com.fisioclinic.repository;

import com.fisioclinic.model.Alta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AltaRepository — Repositório JPA para a entidade Alta (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * findByPacienteId(): histórico de altas do paciente, mais recente primeiro.
 * findByIdWithRelations(): busca alta por id com JOIN FETCH de todas as
 *   relações — evita LazyInitializationException fora do contexto JPA.
 * existsByPacienteIdAndPlanoTratamentoId(): impede registro de duas altas
 *   para o mesmo plano do mesmo paciente.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface AltaRepository extends JpaRepository<Alta, UUID> {

    @Query("SELECT a FROM Alta a JOIN FETCH a.paciente " +
           "LEFT JOIN FETCH a.planoTratamento " +
           "LEFT JOIN FETCH a.fisioterapeuta " +
           "WHERE a.paciente.id = :pacienteId ORDER BY a.dataAlta DESC")
    List<Alta> findByPacienteId(@Param("pacienteId") UUID pacienteId);

    @Query("SELECT a FROM Alta a JOIN FETCH a.paciente " +
           "LEFT JOIN FETCH a.planoTratamento " +
           "LEFT JOIN FETCH a.fisioterapeuta " +
           "WHERE a.id = :id")
    Optional<Alta> findByIdWithRelations(@Param("id") UUID id);

    boolean existsByPacienteIdAndPlanoTratamentoId(UUID pacienteId, UUID planoTratamentoId);

}
