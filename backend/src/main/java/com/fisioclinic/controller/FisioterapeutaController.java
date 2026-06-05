package com.fisioclinic.controller;

import com.fisioclinic.dto.FisioterapeutaDTO;
import com.fisioclinic.dto.FisioterapeutaResponse;
import com.fisioclinic.service.FisioterapeutaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fisioterapeutas")
@RequiredArgsConstructor
public class FisioterapeutaController {

    private final FisioterapeutaService service;

    /**
     * GET /api/fisioterapeutas
     * Lista todos os fisioterapeutas ordenados por nome.
     */
    @GetMapping
    public ResponseEntity<List<FisioterapeutaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /**
     * GET /api/fisioterapeutas/{id}
     * Retorna um fisioterapeuta pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FisioterapeutaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * POST /api/fisioterapeutas
     * Cadastra um novo fisioterapeuta. Senha inicial: "Fisio@123" se não informada.
     */
    @PostMapping
    public ResponseEntity<FisioterapeutaResponse> criar(@Valid @RequestBody FisioterapeutaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    /**
     * PATCH /api/fisioterapeutas/{id}
     * Atualização parcial — apenas campos não-nulos são modificados.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<FisioterapeutaResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody FisioterapeutaDTO dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    /**
     * PATCH /api/fisioterapeutas/{id}/status
     * Ativa ou desativa o acesso do fisioterapeuta ao sistema.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
        @PathVariable UUID id,
        @RequestBody Map<String, Boolean> body
    ) {
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        service.alterarStatus(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
