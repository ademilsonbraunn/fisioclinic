package com.fisioclinic.controller;

import com.fisioclinic.dto.AltaDTO;
import com.fisioclinic.dto.AltaResponse;
import com.fisioclinic.service.AltaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AltaController — Encerramento de tratamento e relatórios (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/altas
 * Acesso: requer autenticação (qualquer perfil)
 *
 * A alta é o evento terminal do ciclo clínico — consome dados de todos os
 * módulos anteriores (M1 paciente, M3 plano, M4 sessões, M5 evoluções).
 * Ao registrar uma alta vinculada a um plano, o serviço marca o plano
 * como "concluido" automaticamente.
 *
 * Endpoints:
 *  POST /api/altas                          → registra alta (201)
 *  GET  /api/altas/paciente/{pacienteId}    → lista altas do paciente (200)
 *  GET  /api/altas/{id}                     → busca alta por ID (200)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/altas")
@RequiredArgsConstructor
public class AltaController {

    private final AltaService altaService;

    /**
     * POST /api/altas
     * Registra a alta do paciente. Lança 409 se já existe alta para o mesmo plano.
     */
    @PostMapping
    public ResponseEntity<AltaResponse> registrar(@Valid @RequestBody AltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(altaService.registrar(dto));
    }

    /**
     * GET /api/altas/paciente/{pacienteId}
     * Retorna o histórico de altas de um paciente, ordenado por data desc.
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<AltaResponse>> listarPorPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(altaService.listarPorPaciente(pacienteId));
    }

    /**
     * GET /api/altas/{id}
     * Retorna uma alta específica com todos os dados relacionados.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AltaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(altaService.buscarPorId(id));
    }
}
