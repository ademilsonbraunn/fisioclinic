package com.fisioclinic.repository;

import com.fisioclinic.model.PlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PlanoTratamentoRepository — Repositório JPA para a entidade PlanoTratamento
 * ─────────────────────────────────────────────────────────────────────────────
 * findByPacienteId(): lista todos os planos do paciente, mais recente primeiro.
 * findByPacienteIdAndStatus(): filtra por status — usado pelo frontend para
 *   exibir apenas planos "ativo" no prontuário (ignora concluídos/suspensos).
 * Ambas carregam fisioterapeuta e anamnese via JOIN FETCH para evitar N+1.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, UUID> {

    @Query("SELECT p FROM PlanoTratamento p " +
           "LEFT JOIN FETCH p.fisioterapeuta " +
           "LEFT JOIN FETCH p.anamnese " +
           "WHERE p.paciente.id = :pacienteId " +
           "ORDER BY p.createdAt DESC")
    List<PlanoTratamento> findByPacienteId(UUID pacienteId);

    @Query("SELECT p FROM PlanoTratamento p " +
           "LEFT JOIN FETCH p.fisioterapeuta " +
           "LEFT JOIN FETCH p.anamnese " +
           "WHERE p.paciente.id = :pacienteId AND p.status = :status " +
           "ORDER BY p.createdAt DESC")
    List<PlanoTratamento> findByPacienteIdAndStatus(UUID pacienteId, String status);
}
