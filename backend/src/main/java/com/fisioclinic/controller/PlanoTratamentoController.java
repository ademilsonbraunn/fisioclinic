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
