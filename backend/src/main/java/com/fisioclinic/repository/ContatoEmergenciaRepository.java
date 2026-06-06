package com.fisioclinic.repository;

import com.fisioclinic.model.ContatoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ContatoEmergenciaRepository — Repositório JPA para ContatoEmergencia
 * ─────────────────────────────────────────────────────────────────────────────
 * findByPacienteId(): busca o contato de um paciente (relação 1:1).
 * findByPacienteIdIn(): carregamento em lote — evita N+1 ao listar pacientes
 *   com seus contatos em uma única query IN.
 * deleteByPacienteId(): usado antes de recriar o contato na atualização do
 *   cadastro do paciente (estratégia delete+insert para simplificar o update).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, UUID> {

    Optional<ContatoEmergencia> findByPacienteId(UUID pacienteId);

    List<ContatoEmergencia> findByPacienteIdIn(Collection<UUID> ids);

    void deleteByPacienteId(UUID pacienteId);
}
