package com.fisioclinic.repository;

import com.fisioclinic.model.ConvenioPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConvenioPacienteRepository extends JpaRepository<ConvenioPaciente, UUID> {

    Optional<ConvenioPaciente> findFirstByPacienteIdOrderByIdAsc(UUID pacienteId);

    void deleteByPacienteId(UUID pacienteId);
}
