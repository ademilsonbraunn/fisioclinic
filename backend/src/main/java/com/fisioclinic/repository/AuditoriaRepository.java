package com.fisioclinic.repository;

import com.fisioclinic.model.AuditoriaProntuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * AuditoriaRepository — Repositório JPA para a trilha de auditoria do prontuário (P2)
 * [Auditoria] Busca eventos do prontuário de um paciente, mais recentes primeiro.
 */
public interface AuditoriaRepository extends JpaRepository<AuditoriaProntuario, UUID> {

    // [Auditoria] JOIN FETCH para evitar N+1 ao serializar fisioterapeuta
    @Query("SELECT a FROM AuditoriaProntuario a LEFT JOIN FETCH a.fisioterapeuta " +
           "WHERE a.paciente.id = :pacienteId ORDER BY a.createdAt DESC")
    List<AuditoriaProntuario> findByPacienteId(@Param("pacienteId") UUID pacienteId);
}
