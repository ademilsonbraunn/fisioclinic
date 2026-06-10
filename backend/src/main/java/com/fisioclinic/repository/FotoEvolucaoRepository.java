package com.fisioclinic.repository;

import com.fisioclinic.model.FotoEvolucao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * FotoEvolucaoRepository — Acesso à tabela fotos_evolucao (M5 P2)
 * [M5] Busca fotos vinculadas a uma evolução, ordenadas por data de criação.
 */
public interface FotoEvolucaoRepository extends JpaRepository<FotoEvolucao, UUID> {

    // [M5 P2] Galeria ordenada cronologicamente para exibição no card de evolução
    List<FotoEvolucao> findByEvolucaoIdOrderByCreatedAtAsc(UUID evolucaoId);
}
