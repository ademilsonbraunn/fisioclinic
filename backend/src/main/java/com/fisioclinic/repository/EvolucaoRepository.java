package com.fisioclinic.repository;

import com.fisioclinic.model.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
