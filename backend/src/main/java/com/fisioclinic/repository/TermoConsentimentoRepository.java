package com.fisioclinic.repository;

import com.fisioclinic.model.TermoConsentimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimentoRepository — Repositório JPA para TCLE (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface TermoConsentimentoRepository extends JpaRepository<TermoConsentimento, UUID> {

    @Query("SELECT t FROM TermoConsentimento t LEFT JOIN FETCH t.plano WHERE t.plano.id = :planoId ORDER BY t.assinadoEm DESC")
    List<TermoConsentimento> findByPlanoId(UUID planoId);

    @Query("SELECT t FROM TermoConsentimento t LEFT JOIN FETCH t.plano WHERE t.paciente.id = :pacienteId ORDER BY t.assinadoEm DESC")
    List<TermoConsentimento> findByPacienteIdOrderByAssinadoEmDesc(UUID pacienteId);
}
