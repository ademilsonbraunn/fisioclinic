package com.fisioclinic.repository;

import com.fisioclinic.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SalaRepository — Repositório JPA para a entidade Sala
 * ─────────────────────────────────────────────────────────────────────────────
 * findAllByOrderByNomeAsc(): lista todas as salas (incluindo inativas)
 *   — usada na tela de administração.
 * findByAtivoTrueOrderByNomeAsc(): lista apenas salas disponíveis para
 *   seleção no agendamento.
 * existsByNomeIgnoreCase(): evita duplicatas de nomes com casing diferente
 *   (ex: "Box 1" e "box 1").
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface SalaRepository extends JpaRepository<Sala, UUID> {

    List<Sala> findAllByOrderByNomeAsc();

    List<Sala> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
