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

@RestController
@RequestMapping("/api/evolucoes")
@RequiredArgsConstructor
public class EvolucaoController {

    private final EvolucaoService evolucaoService;

    @PostMapping
    public ResponseEntity<EvolucaoResponse> criar(@Valid @RequestBody EvolucaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evolucaoService.criar(dto));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<EvolucaoResponse>> listarPorPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(evolucaoService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/sessao/{sessaoId}")
    public ResponseEntity<EvolucaoResponse> buscarPorSessao(@PathVariable UUID sessaoId) {
        return ResponseEntity.ok(evolucaoService.buscarPorSessao(sessaoId));
    }
}
