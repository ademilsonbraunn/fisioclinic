package com.fisioclinic.repository;

import com.fisioclinic.model.AtualizacaoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtualizacaoSistemaRepository extends JpaRepository<AtualizacaoSistema, UUID> {

    List<AtualizacaoSistema> findTop10ByAtivoTrueOrderByDataLancamentoDesc();
}
