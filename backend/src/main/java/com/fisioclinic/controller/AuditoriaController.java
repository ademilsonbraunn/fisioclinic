package com.fisioclinic.controller;

import com.fisioclinic.dto.AuditoriaResponse;
import com.fisioclinic.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AuditoriaController — Trilha de eventos do prontuário (Auditoria — P2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/auditoria
 * Acesso: requer autenticação (qualquer perfil)
 *
 * [Auditoria CFM] Exposição da trilha de auditoria para conformidade com
 * a Resolução CFM 1.821/07. Dados clínicos sensíveis não são retornados
 * (LGPD art. 11) — apenas metadados de quem fez o quê e quando.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // GET /api/auditoria/paciente/{id} — lista eventos ordenados por data DESC
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<AuditoriaResponse>> listarPorPaciente(
        @PathVariable UUID pacienteId
    ) {
        return ResponseEntity.ok(auditoriaService.listarPorPaciente(pacienteId));
    }
}
