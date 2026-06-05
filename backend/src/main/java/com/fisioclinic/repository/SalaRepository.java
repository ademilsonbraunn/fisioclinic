package com.fisioclinic.repository;

import com.fisioclinic.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    List<Sala> findAllByOrderByNomeAsc();

    List<Sala> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
