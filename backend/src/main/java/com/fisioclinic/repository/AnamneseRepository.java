package com.fisioclinic.repository;

import com.fisioclinic.model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AnamneseRepository — Repositório JPA para a entidade Anamnese
 * ─────────────────────────────────────────────────────────────────────────────
 * findByPacienteId(): histórico completo de avaliações do paciente, mais
 *   recente primeiro.
 * findUltimaByPacienteId(): busca a avaliação mais recente — usada pelo
 *   PlanoTratamentoService para pré-preencher o diagnóstico com base na
 *   última anamnese.
 * Ambas usam JOIN FETCH para evitar N+1 ao carregar paciente e fisioterapeuta.
 * ─────────────────────────────────────────────────────────────────────────────
 */
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
