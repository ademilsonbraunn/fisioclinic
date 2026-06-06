package com.fisioclinic.repository;

import com.fisioclinic.model.AtualizacaoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AtualizacaoSistemaRepository — Repositório JPA para AtualizacaoSistema
 * ─────────────────────────────────────────────────────────────────────────────
 * findTop10ByAtivoTrueOrderByDataLancamentoDesc(): retorna as 10 atualizações
 *   mais recentes com ativo=true — exibidas no card de novidades do dashboard.
 *   O limite de 10 é suficiente para o scroll do card sem paginar.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface AtualizacaoSistemaRepository extends JpaRepository<AtualizacaoSistema, UUID> {

    List<AtualizacaoSistema> findTop10ByAtivoTrueOrderByDataLancamentoDesc();
}
