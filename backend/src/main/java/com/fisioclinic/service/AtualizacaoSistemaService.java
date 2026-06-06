package com.fisioclinic.service;

import com.fisioclinic.dto.AtualizacaoSistemaResponse;
import com.fisioclinic.repository.AtualizacaoSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AtualizacaoSistemaService — Changelog de novidades do sistema
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Serviço simples de leitura: retorna as 10 atualizações mais recentes com
 * ativo=true, ordenadas por data_lancamento decrescente.
 *
 * Registros são inseridos manualmente via SQL (INSERT INTO atualizacoes_sistema)
 * sempre que um módulo novo é concluído, conforme documentado no CLAUDE.md.
 * Este service não oferece endpoints de escrita — gestão é feita pelo DBA/admin.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
public class AtualizacaoSistemaService {

    private final AtualizacaoSistemaRepository repository;

    @Transactional(readOnly = true)
    public List<AtualizacaoSistemaResponse> listar() {
        return repository.findTop10ByAtivoTrueOrderByDataLancamentoDesc()
                .stream()
                .map(AtualizacaoSistemaResponse::from)
                .toList();
    }
}
