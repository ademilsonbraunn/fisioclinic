package com.fisioclinic.controller;

import com.fisioclinic.dto.FotoEvolucaoResponse;
import com.fisioclinic.service.FotoEvolucaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FotoEvolucaoController — Fotos comparativas antes/depois (Módulo 5 — P2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/evolucoes/{evolucaoId}/fotos
 * Acesso: requer autenticação (qualquer perfil)
 *
 * [M5 P2] Permite vincular fotos before/after a cada sessão de evolução clínica.
 * Padrão idêntico ao AnamneseController (M2) para arquivos de anamnese.
 * Arquivos servidos via endpoint autenticado — não expostos diretamente no disco.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/evolucoes/{evolucaoId}/fotos")
@RequiredArgsConstructor
public class FotoEvolucaoController {

    private final FotoEvolucaoService fotoService;

    // POST /api/evolucoes/{evolucaoId}/fotos  (multipart: file + tipo)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoEvolucaoResponse> uploadFoto(
        @PathVariable UUID evolucaoId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "tipo", defaultValue = "outro") String tipo
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fotoService.salvar(evolucaoId, file, tipo));
    }

    // GET /api/evolucoes/{evolucaoId}/fotos
    @GetMapping
    public ResponseEntity<List<FotoEvolucaoResponse>> listarFotos(@PathVariable UUID evolucaoId) {
        return ResponseEntity.ok(fotoService.listar(evolucaoId));
    }

    // GET /api/evolucoes/{evolucaoId}/fotos/{fotoId}/arquivo  — serve o binário da imagem
    @GetMapping("/{fotoId}/arquivo")
    public ResponseEntity<byte[]> servirFoto(
        @PathVariable UUID fotoId
    ) {
        byte[] bytes = fotoService.download(fotoId);
        // [M5 P2] APPLICATION_OCTET_STREAM — browser exibe imagem corretamente (padrão de ArquivoAnamnese)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
    }

    // DELETE /api/evolucoes/{evolucaoId}/fotos/{fotoId}
    @DeleteMapping("/{fotoId}")
    public ResponseEntity<Void> deletarFoto(
        @PathVariable UUID evolucaoId,
        @PathVariable UUID fotoId
    ) {
        fotoService.deletar(fotoId);
        return ResponseEntity.noContent().build();
    }

}
