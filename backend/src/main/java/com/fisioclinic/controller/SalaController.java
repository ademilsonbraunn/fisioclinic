package com.fisioclinic.controller;

import com.fisioclinic.dto.SalaDTO;
import com.fisioclinic.dto.SalaResponse;
import com.fisioclinic.service.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SalaController — Gestão de salas e boxes de atendimento
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/salas
 * Acesso: requer ROLE_ADMIN
 *
 * Salas são usadas pelo Módulo 4 (Agendamento) — toda sessão referencia uma sala.
 * A regra de negócio de conflito (mesma sala, mesmo horário) vive no SessaoService.
 *
 * Endpoints:
 *  GET  /api/salas         → lista todas (ativas e inativas)
 *  GET  /api/salas/ativas  → apenas salas ativas — usado nos formulários de agendamento
 *  GET  /api/salas/{id}    → busca por ID
 *  POST /api/salas         → cadastra nova sala/box
 *  PATCH /api/salas/{id}   → atualização parcial (inclui ativar/desativar)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/salas")
@RequiredArgsConstructor
public class SalaController {

    private final SalaService service;

    /**
     * GET /api/salas
     * Lista todas as salas (ativas e inativas), ordenadas por nome.
     */
    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /**
     * GET /api/salas/ativas
     * Lista apenas salas ativas — usado no agendamento.
     */
    @GetMapping("/ativas")
    public ResponseEntity<List<SalaResponse>> listarAtivas() {
        return ResponseEntity.ok(service.listarAtivas());
    }

    /**
     * GET /api/salas/{id}
     * Retorna uma sala pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * POST /api/salas
     * Cadastra uma nova sala ou box.
     */
    @PostMapping
    public ResponseEntity<SalaResponse> criar(@Valid @RequestBody SalaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    /**
     * PATCH /api/salas/{id}
     * Atualização parcial — apenas campos não-nulos são modificados.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<SalaResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody SalaDTO dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }
}
