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
