package com.fisioclinic.controller;

import com.fisioclinic.dto.PacienteDTO;
import com.fisioclinic.dto.PacienteResponse;
import com.fisioclinic.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

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
}
