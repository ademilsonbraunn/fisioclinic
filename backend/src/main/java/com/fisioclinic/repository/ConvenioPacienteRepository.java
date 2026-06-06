package com.fisioclinic.repository;

import com.fisioclinic.model.ConvenioPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ConvenioPacienteRepository — Repositório JPA para ConvenioPaciente
 * ─────────────────────────────────────────────────────────────────────────────
 * findFirstByPacienteIdOrderByIdAsc(): retorna o convênio ativo do paciente.
 *   O "First" é defensivo — o schema permite apenas um registro por paciente,
 *   mas garante determinismo se houver duplicatas de dados legados.
 * findByPacienteIdIn(): carregamento em lote — evita N+1 ao listar pacientes
 *   com seus dados de convênio em uma única query IN.
 * deleteByPacienteId(): estratégia delete+insert na atualização do cadastro
 *   (mesma abordagem do ContatoEmergenciaRepository).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface ConvenioPacienteRepository extends JpaRepository<ConvenioPaciente, UUID> {

    Optional<ConvenioPaciente> findFirstByPacienteIdOrderByIdAsc(UUID pacienteId);

    List<ConvenioPaciente> findByPacienteIdIn(Collection<UUID> ids);

    void deleteByPacienteId(UUID pacienteId);
}
