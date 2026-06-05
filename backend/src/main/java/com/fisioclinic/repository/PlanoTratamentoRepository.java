package com.fisioclinic.repository;

import com.fisioclinic.model.PlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
