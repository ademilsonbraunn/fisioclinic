package com.fisioclinic.controller;

import com.fisioclinic.dto.AtualizacaoSistemaResponse;
import com.fisioclinic.service.AtualizacaoSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atualizacoes")
@RequiredArgsConstructor
public class AtualizacaoSistemaController {

    private final AtualizacaoSistemaService service;

    @GetMapping
    public ResponseEntity<List<AtualizacaoSistemaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
