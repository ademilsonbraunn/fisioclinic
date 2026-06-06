package com.fisioclinic.repository;

import com.fisioclinic.model.ConvenioPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvenioPacienteRepository extends JpaRepository<ConvenioPaciente, UUID> {

    Optional<ConvenioPaciente> findFirstByPacienteIdOrderByIdAsc(UUID pacienteId);

    List<ConvenioPaciente> findByPacienteIdIn(Collection<UUID> ids);

    void deleteByPacienteId(UUID pacienteId);
}
