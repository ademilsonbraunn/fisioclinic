package com.fisioclinic.controller;

import com.fisioclinic.dto.AnamneseDTO;
import com.fisioclinic.dto.AnamneseResponse;
import com.fisioclinic.service.AnamneseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AnamneseController — Anamnese e avaliação inicial (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/anamneses
 * Acesso: requer autenticação (qualquer perfil)
 *
 * A anamnese é o primeiro documento clínico do paciente: registra queixa,
 * histórico e avaliação física. Seu ID é referenciado pelo Módulo 3
 * (PlanoTratamento) para contextualizar o diagnóstico.
 *
 * O campo avaliacao_fisica é armazenado como JSONB no PostgreSQL, permitindo
 * dados de avaliação com estrutura dinâmica (postura, ADM, força, EVA, etc.).
 *
 * Endpoints:
 *  GET   /api/anamneses?paciente_id=UUID → todas as anamneses do paciente
 *  GET   /api/anamneses/{id}             → busca por ID
 *  POST  /api/anamneses                  → registra nova anamnese
 *  PATCH /api/anamneses/{id}             → atualização parcial
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/anamneses")
@RequiredArgsConstructor
public class AnamneseController {

    private final AnamneseService anamneseService;

    // GET /api/anamneses?paciente_id=UUID
    @GetMapping
    public ResponseEntity<List<AnamneseResponse>> listar(
        @RequestParam(name = "paciente_id") UUID pacienteId
    ) {
        return ResponseEntity.ok(anamneseService.listarPorPaciente(pacienteId));
    }

    // GET /api/anamneses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(anamneseService.buscarPorId(id));
    }

    // POST /api/anamneses
    @PostMapping
    public ResponseEntity<AnamneseResponse> criar(@Valid @RequestBody AnamneseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anamneseService.criar(dto));
    }

    // PATCH /api/anamneses/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<AnamneseResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody AnamneseDTO dto
    ) {
        return ResponseEntity.ok(anamneseService.atualizar(id, dto));
    }
}
