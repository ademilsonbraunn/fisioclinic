package com.fisioclinic.controller;

import com.fisioclinic.dto.PlanoTratamentoDTO;
import com.fisioclinic.dto.PlanoTratamentoResponse;
import com.fisioclinic.service.PlanoTratamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PlanoTratamentoController — Diagnóstico e plano de tratamento (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/planos
 * Acesso: requer autenticação (qualquer perfil)
 *
 * O plano de tratamento formaliza o diagnóstico fisioterapêutico (CIF),
 * os objetivos e as técnicas escolhidas. Pode ser vinculado a uma anamnese (M2).
 * Seus campos frequencia_semanal e num_sessoes_estimado orientam o agendamento (M4).
 *
 * Status válidos: "ativo" | "concluido" | "cancelado"
 *
 * Endpoints:
 *  GET   /api/planos?paciente_id=UUID → todos os planos do paciente
 *  GET   /api/planos/{id}             → busca por ID
 *  POST  /api/planos                  → cria novo plano
 *  PATCH /api/planos/{id}             → atualização parcial
 *  PATCH /api/planos/{id}/status      → muda status via body {"status": "concluido"}
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoTratamentoController {

    private final PlanoTratamentoService planoService;

    // GET /api/planos?paciente_id=UUID
    @GetMapping
    public ResponseEntity<List<PlanoTratamentoResponse>> listar(
        @RequestParam(name = "paciente_id") UUID pacienteId
    ) {
        return ResponseEntity.ok(planoService.listarPorPaciente(pacienteId));
    }

    // GET /api/planos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PlanoTratamentoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(planoService.buscarPorId(id));
    }

    // POST /api/planos
    @PostMapping
    public ResponseEntity<PlanoTratamentoResponse> criar(@Valid @RequestBody PlanoTratamentoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoService.criar(dto));
    }

    // PATCH /api/planos/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<PlanoTratamentoResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody PlanoTratamentoDTO dto
    ) {
        return ResponseEntity.ok(planoService.atualizar(id, dto));
    }

    // PATCH /api/planos/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<PlanoTratamentoResponse> atualizarStatus(
        @PathVariable UUID id,
        @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(planoService.atualizarStatus(id, status));
    }
}
