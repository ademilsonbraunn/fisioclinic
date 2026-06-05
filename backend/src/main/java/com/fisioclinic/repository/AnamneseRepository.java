package com.fisioclinic.repository;

import com.fisioclinic.model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnamneseRepository extends JpaRepository<Anamnese, UUID> {

    @Query("""
        SELECT a FROM Anamnese a
        JOIN FETCH a.paciente
        LEFT JOIN FETCH a.fisioterapeuta
        WHERE a.paciente.id = :pacienteId
        ORDER BY a.createdAt DESC
    """)
    List<Anamnese> findByPacienteId(@Param("pacienteId") UUID pacienteId);

    @Query("""
        SELECT a FROM Anamnese a
        JOIN FETCH a.paciente
        LEFT JOIN FETCH a.fisioterapeuta
        WHERE a.paciente.id = :pacienteId
        ORDER BY a.createdAt DESC
        LIMIT 1
    """)
    Optional<Anamnese> findUltimaByPacienteId(@Param("pacienteId") UUID pacienteId);
}
