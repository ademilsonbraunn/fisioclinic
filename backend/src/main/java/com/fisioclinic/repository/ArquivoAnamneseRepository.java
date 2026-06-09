package com.fisioclinic.repository;

import com.fisioclinic.model.ArquivoAnamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ArquivoAnamneseRepository — Repositório JPA para arquivos da anamnese (M2)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface ArquivoAnamneseRepository extends JpaRepository<ArquivoAnamnese, UUID> {

    List<ArquivoAnamnese> findByAnamneseIdOrderByCreatedAtDesc(UUID anamneseId);
}
