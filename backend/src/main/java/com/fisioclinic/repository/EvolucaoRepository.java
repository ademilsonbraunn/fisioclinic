package com.fisioclinic.repository;

import com.fisioclinic.model.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EvolucaoRepository — Repositório JPA para a entidade Evolucao
 * ─────────────────────────────────────────────────────────────────────────────
 * findByPacienteId(): histórico completo de evoluções do paciente, mais
 *   recente primeiro — usado na timeline do prontuário.
 * findBySessaoId(): busca a evolução de uma sessão específica. Retorna
 *   Optional pois nem toda sessão tem evolução registrada ainda.
 * existsBySessaoId(): verificação rápida antes de criar nova evolução —
 *   impõe a regra "apenas uma evolução por sessão".
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface EvolucaoRepository extends JpaRepository<Evolucao, UUID> {

    @Query("SELECT e FROM Evolucao e JOIN FETCH e.sessao JOIN FETCH e.paciente " +
           "LEFT JOIN FETCH e.fisioterapeuta " +
           "WHERE e.paciente.id = :pacienteId ORDER BY e.dataHora DESC")
    List<Evolucao> findByPacienteId(@Param("pacienteId") UUID pacienteId);

    @Query("SELECT e FROM Evolucao e JOIN FETCH e.sessao JOIN FETCH e.paciente " +
           "LEFT JOIN FETCH e.fisioterapeuta " +
           "WHERE e.sessao.id = :sessaoId")
    Optional<Evolucao> findBySessaoId(@Param("sessaoId") UUID sessaoId);

    boolean existsBySessaoId(UUID sessaoId);
}
