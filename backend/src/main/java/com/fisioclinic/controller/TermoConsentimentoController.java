package com.fisioclinic.controller;

import com.fisioclinic.dto.TermoConsentimentoDTO;
import com.fisioclinic.dto.TermoConsentimentoResponse;
import com.fisioclinic.service.TermoConsentimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimentoController — TCLE e termos de consentimento (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/termos
 * Acesso: requer autenticação (qualquer perfil)
 *
 * Registra o consentimento do paciente ao plano de tratamento com timestamp
 * de assinatura, conforme Resolução CFM 1.821/07.
 *
 * Endpoints:
 *  POST /api/termos               → registra novo TCLE
 *  GET  /api/termos/plano/{id}    → lista termos de um plano
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/termos")
@RequiredArgsConstructor
public class TermoConsentimentoController {

    private final TermoConsentimentoService termoService;

    // POST /api/termos
    @PostMapping
    public ResponseEntity<TermoConsentimentoResponse> registrar(
        @Valid @RequestBody TermoConsentimentoDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termoService.registrar(dto));
    }

    // GET /api/termos/plano/{planoId}
    @GetMapping("/plano/{planoId}")
    public ResponseEntity<List<TermoConsentimentoResponse>> listarPorPlano(
        @PathVariable UUID planoId
    ) {
        return ResponseEntity.ok(termoService.listarPorPlano(planoId));
    }
}
