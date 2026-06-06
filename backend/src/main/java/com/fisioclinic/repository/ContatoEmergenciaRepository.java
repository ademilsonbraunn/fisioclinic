package com.fisioclinic.repository;

import com.fisioclinic.model.ContatoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, UUID> {

    Optional<ContatoEmergencia> findByPacienteId(UUID pacienteId);

    List<ContatoEmergencia> findByPacienteIdIn(Collection<UUID> ids);

    void deleteByPacienteId(UUID pacienteId);
}
