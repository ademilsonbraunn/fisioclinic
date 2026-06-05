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

@RestController
@RequestMapping("/api/sessoes")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoService sessaoService;

    @GetMapping
    public ResponseEntity<List<SessaoResponse>> listar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data_fim
    ) {
        return ResponseEntity.ok(sessaoService.listar(data_inicio, data_fim));
    }

    @GetMapping("/semana")
    public ResponseEntity<List<SessaoResponse>> listarSemana() {
        return ResponseEntity.ok(sessaoService.listarSemana());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessaoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(sessaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SessaoResponse> criar(@Valid @RequestBody SessaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessaoService.criar(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SessaoResponse> atualizar(
        @PathVariable UUID id,
        @RequestBody SessaoDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SessaoResponse> atualizarStatus(
        @PathVariable UUID id,
        @Valid @RequestBody StatusDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.atualizarStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        sessaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
