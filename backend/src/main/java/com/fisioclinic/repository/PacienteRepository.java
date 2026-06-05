package com.fisioclinic.repository;

import com.fisioclinic.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    boolean existsByCpf(String cpf);

    Optional<Paciente> findByCpf(String cpf);

    @Query("""
        SELECT p FROM Paciente p
        WHERE (:busca IS NULL
               OR LOWER(p.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR p.cpf LIKE CONCAT('%', :busca, '%'))
        ORDER BY p.nomeCompleto ASC
        """)
    List<Paciente> buscar(@Param("busca") String busca);
}
