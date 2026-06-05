package com.fisioclinic.controller;

import com.fisioclinic.dto.SalaDTO;
import com.fisioclinic.dto.SalaResponse;
import com.fisioclinic.service.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salas")
@RequiredArgsConstructor
public class SalaController {

    private final SalaService service;

    /**
     * GET /api/salas
     * Lista todas as salas (ativas e inativas), ordenadas por nome.
     */
    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /**
     * GET /api/salas/ativas
     * Lista apenas salas ativas — usado no agendamento.
     */
    @GetMapping("/ativas")
    public ResponseEntity<List<SalaResponse>> listarAtivas() {
        return ResponseEntity.ok(service.listarAtivas());
    }

    /**
     * GET /api/salas/{id}
     * Retorna uma sala pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * POST /api/salas
     * Cadastra uma nova sala ou box.
     */
    @PostMapping
    public ResponseEntity<SalaResponse> criar(@Valid @RequestBody SalaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    /**
     * PATCH /api/salas/{id}
     * Atualização parcial — apenas campos não-nulos são modificados.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<SalaResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody SalaDTO dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }
}
