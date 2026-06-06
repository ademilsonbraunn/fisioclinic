package com.fisioclinic.controller;

import com.fisioclinic.dto.SessaoDTO;
import com.fisioclinic.dto.SessaoResponse;
import com.fisioclinic.dto.StatusDTO;
import com.fisioclinic.service.SessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SessaoController — Agendamento de sessões (Módulo 4)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/sessoes
 * Acesso: requer autenticação (qualquer perfil)
 *
 * Sessões são o elo central entre os módulos: conectam paciente (M1),
 * fisioterapeuta e sala (cadastros admin) com a evolução clínica (M5).
 *
 * Endpoints:
 *  GET    /api/sessoes                → lista com filtros opcionais por data ou paciente
 *  GET    /api/sessoes/semana         → sessões da semana atual (seg a dom)
 *  GET    /api/sessoes/{id}           → busca por ID
 *  POST   /api/sessoes                → cria nova sessão (valida conflito de sala)
 *  PATCH  /api/sessoes/{id}           → edita dados da sessão
 *  PATCH  /api/sessoes/{id}/status    → muda status (AGENDADO→REALIZADO, etc.)
 *  DELETE /api/sessoes/{id}           → remove sessão (só permitido via admin)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/sessoes")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoService sessaoService;

    /**
     * GET /api/sessoes?data_inicio=YYYY-MM-DD&data_fim=YYYY-MM-DD&paciente_id=UUID
     * Quando paciente_id é informado, ignora os outros filtros e retorna todas as sessões do paciente.
     * Quando nenhum filtro é passado, retorna a semana atual.
     */
    @GetMapping
    public ResponseEntity<List<SessaoResponse>> listar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_fim,
        @RequestParam(required = false) UUID paciente_id
    ) {
        return ResponseEntity.ok(sessaoService.listar(data_inicio, data_fim, paciente_id));
    }

    /**
     * GET /api/sessoes/semana
     * Retorna as sessões da semana corrente (segunda a domingo).
     * Atalho conveniente para o calendário da agenda.
     */
    @GetMapping("/semana")
    public ResponseEntity<List<SessaoResponse>> listarSemana() {
        return ResponseEntity.ok(sessaoService.listarSemana());
    }

    /**
     * GET /api/sessoes/{id}
     * Retorna os dados completos de uma sessão (paciente, fisio, sala, status).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessaoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(sessaoService.buscarPorId(id));
    }

    /**
     * POST /api/sessoes
     * Cria nova sessão. Valida: intervalo de horário, existência de paciente/fisio/sala
     * e conflito de sala (dois agendamentos no mesmo horário).
     */
    @PostMapping
    public ResponseEntity<SessaoResponse> criar(@Valid @RequestBody SessaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessaoService.criar(dto));
    }

    /**
     * PATCH /api/sessoes/{id}
     * Atualização parcial — apenas campos não-nulos são modificados.
     * Revalida conflito de sala ao alterar horário.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<SessaoResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody SessaoDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.atualizar(id, dto));
    }

    /**
     * PATCH /api/sessoes/{id}/status
     * Muda apenas o status da sessão. Se status = CANCELADO, motivo é obrigatório.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SessaoResponse> atualizarStatus(
        @PathVariable UUID id,
        @Valid @RequestBody StatusDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.atualizarStatus(id, dto));
    }

    /**
     * DELETE /api/sessoes/{id}
     * Remove a sessão permanentemente. Retorna 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        sessaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
