package com.fisioclinic.controller;

import com.fisioclinic.dto.AtualizacaoSistemaResponse;
import com.fisioclinic.service.AtualizacaoSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AtualizacaoSistemaController — Novidades e changelog do sistema
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/atualizacoes
 * Acesso: requer autenticação (qualquer perfil)
 *
 * Fornece as últimas 10 atualizações visíveis do sistema para o painel de
 * notificações no topbar do frontend. Registros são inseridos via SQL diretamente
 * na tabela atualizacoes_sistema sempre que um módulo é concluído.
 *
 * Tipos de atualização: NOVO_RECURSO | MELHORIA | CORRECAO
 *
 * Endpoint:
 *  GET /api/atualizacoes → retorna as 10 mais recentes com ativo=true
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/atualizacoes")
@RequiredArgsConstructor
public class AtualizacaoSistemaController {

    private final AtualizacaoSistemaService service;

    /**
     * GET /api/atualizacoes
     * Retorna as 10 atualizações mais recentes ativas, ordenadas por data_lancamento desc.
     */
    @GetMapping
    public ResponseEntity<List<AtualizacaoSistemaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
