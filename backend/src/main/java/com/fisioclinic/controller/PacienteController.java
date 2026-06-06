package com.fisioclinic.controller;

import com.fisioclinic.dto.PacienteDTO;
import com.fisioclinic.dto.PacienteResponse;
import com.fisioclinic.dto.SessaoResponse;
import com.fisioclinic.service.PacienteService;
import com.fisioclinic.service.SessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PacienteController — API REST de pacientes (Módulo 1)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/pacientes
 * Acesso: público (sem autenticação) — aguarda integração completa do JWT no frontend
 *
 * Responsabilidades:
 *  - Receber requisições HTTP, validar DTOs e delegar ao PacienteService
 *  - Devolver ResponseEntity com o status HTTP correto (200, 201, 409…)
 *  - NÃO contém regras de negócio — apenas roteamento e conversão de tipos
 *
 * Dependências injetadas:
 *  - PacienteService: CRUD e validações de pacientes
 *  - SessaoService: listagem de sessões por paciente (cross-module M1→M4)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final SessaoService   sessaoService;

    /**
     * GET /api/pacientes?busca=termo
     * Lista todos os pacientes, com busca opcional por nome ou CPF.
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar(
        @RequestParam(required = false) String busca
    ) {
        return ResponseEntity.ok(pacienteService.listar(busca));
    }

    /**
     * GET /api/pacientes/{id}
     * Retorna um paciente pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    /**
     * POST /api/pacientes
     * Cadastra um novo paciente. Campos obrigatórios são validados via @Valid.
     */
    @PostMapping
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteDTO dto) {
        PacienteResponse response = pacienteService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/pacientes/{id}
     * Atualização parcial — apenas os campos enviados (não-nulos) são modificados.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody PacienteDTO dto
    ) {
        return ResponseEntity.ok(pacienteService.atualizar(id, dto));
    }

    /**
     * GET /api/pacientes/{id}/sessoes
     * Cross-module M1→M4: retorna todas as sessões agendadas para um paciente.
     * Usado no prontuário para montar o histórico de atendimentos.
     */
    @GetMapping("/{id}/sessoes")
    public ResponseEntity<List<SessaoResponse>> listarSessoes(@PathVariable UUID id) {
        return ResponseEntity.ok(sessaoService.listarPorPaciente(id));
    }
}
