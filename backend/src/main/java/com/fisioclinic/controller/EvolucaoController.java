package com.fisioclinic.controller;

import com.fisioclinic.dto.EvolucaoDTO;
import com.fisioclinic.dto.EvolucaoResponse;
import com.fisioclinic.service.EvolucaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EvolucaoController — Evolução clínica SOAP (Módulo 5)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/evolucoes
 * Acesso: requer autenticação (qualquer perfil)
 *
 * Cada evolução é vinculada a exatamente uma sessão (sessao_id) — regra de
 * negócio: não pode haver duas evoluções para a mesma sessão (ConflictException).
 * O paciente é derivado da própria sessão (não precisa ser informado no DTO).
 *
 * Estrutura SOAP: subjetivo | objetivo | avaliacao_clinica | plano
 * Dados complementares: técnicas (JSON), aparelhos, EVA antes/depois, TUSS
 *
 * Endpoints:
 *  POST /api/evolucoes                      → registra evolução (única por sessão)
 *  GET  /api/evolucoes/paciente/{pacienteId} → histórico de evoluções do paciente
 *  GET  /api/evolucoes/sessao/{sessaoId}     → evolução de uma sessão específica
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/evolucoes")
@RequiredArgsConstructor
public class EvolucaoController {

    private final EvolucaoService evolucaoService;

    /**
     * POST /api/evolucoes
     * Registra a evolução SOAP de uma sessão. Lança 409 se a sessão já tem evolução.
     */
    @PostMapping
    public ResponseEntity<EvolucaoResponse> criar(@Valid @RequestBody EvolucaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evolucaoService.criar(dto));
    }

    /**
     * GET /api/evolucoes/paciente/{pacienteId}
     * Retorna todo o histórico de evoluções de um paciente, ordenado por data desc.
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<EvolucaoResponse>> listarPorPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(evolucaoService.listarPorPaciente(pacienteId));
    }

    /**
     * GET /api/evolucoes/sessao/{sessaoId}
     * Retorna a evolução de uma sessão. Lança 404 se não houver evolução registrada.
     */
    @GetMapping("/sessao/{sessaoId}")
    public ResponseEntity<EvolucaoResponse> buscarPorSessao(@PathVariable UUID sessaoId) {
        return ResponseEntity.ok(evolucaoService.buscarPorSessao(sessaoId));
    }
}
