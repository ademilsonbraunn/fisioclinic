package com.fisioclinic.service;

import com.fisioclinic.dto.ArquivoAnamneseResponse;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Anamnese;
import com.fisioclinic.model.ArquivoAnamnese;
import com.fisioclinic.repository.AnamneseRepository;
import com.fisioclinic.repository.ArquivoAnamneseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ArquivoAnamneseService — Upload e gestão de arquivos da anamnese (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Arquivos físicos ficam em: {raiz-da-app}/uploads/anamneses/{anamneseId}/
 * O campo 'url' na entidade armazena o caminho relativo no disco.
 * O frontend acessa os arquivos via endpoint autenticado de download —
 *   nunca servindo diretamente o filesystem (exigência LGPD art. 11).
 *
 * Limite por arquivo: 20 MB. Tipos válidos: exame | laudo | encaminhamento | outro.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ArquivoAnamneseService {

    private static final List<String> TIPOS_VALIDOS =
        List.of("exame", "laudo", "encaminhamento", "outro");

    private static final long MAX_BYTES = 20L * 1024 * 1024; // 20 MB

    private final ArquivoAnamneseRepository arquivoRepository;
    private final AnamneseRepository        anamneseRepository;

    // ── Upload ───────────────────────────────────────────────────────────────

    public ArquivoAnamneseResponse salvar(UUID anamneseId, MultipartFile file, String tipo) {
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException(
                "Tipo inválido. Use: exame, laudo, encaminhamento ou outro");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o limite de 20 MB");
        }

        Anamnese anamnese = anamneseRepository.findById(anamneseId)
            .orElseThrow(() -> new ResourceNotFoundException("Anamnese não encontrada: " + anamneseId));

        try {
            Path dir = diretorioBase().resolve(anamneseId.toString());
            Files.createDirectories(dir);

            // Nome único no disco para evitar sobrescrita
            String nomeOriginal = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "arquivo";
            String nomeSanitizado = UUID.randomUUID() + "_" +
                nomeOriginal.replaceAll("[^a-zA-Z0-9._\\-]", "_");
            file.transferTo(dir.resolve(nomeSanitizado));

            // Caminho relativo no disco: {anamneseId}/{nomeSanitizado}
            String caminhoRelativo = anamneseId + "/" + nomeSanitizado;

            ArquivoAnamnese arquivo = new ArquivoAnamnese();
            arquivo.setAnamnese(anamnese);
            arquivo.setTipo(tipo);
            arquivo.setNomeArquivo(nomeOriginal);
            arquivo.setUrl(caminhoRelativo); // disco — convertido para URL de download em toResponse
            arquivo.setTamanhoBytes(file.getSize());

            return toResponse(arquivoRepository.save(arquivo));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo: " + e.getMessage(), e);
        }
    }

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ArquivoAnamneseResponse> listar(UUID anamneseId) {
        anamneseRepository.findById(anamneseId)
            .orElseThrow(() -> new ResourceNotFoundException("Anamnese não encontrada: " + anamneseId));
        return arquivoRepository.findByAnamneseIdOrderByCreatedAtDesc(anamneseId)
            .stream().map(this::toResponse).toList();
    }

    // ── Download ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] download(UUID arquivoId) {
        ArquivoAnamnese arquivo = arquivoRepository.findById(arquivoId)
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado: " + arquivoId));

        Path caminho = diretorioBase().resolve(arquivo.getUrl());
        if (!Files.exists(caminho)) {
            throw new ResourceNotFoundException("Arquivo físico não encontrado no servidor");
        }
        try {
            return Files.readAllBytes(caminho);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler arquivo: " + e.getMessage(), e);
        }
    }

    // ── Deleção ──────────────────────────────────────────────────────────────

    public void deletar(UUID arquivoId) {
        ArquivoAnamnese arquivo = arquivoRepository.findById(arquivoId)
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado: " + arquivoId));

        // Remove arquivo físico — silencioso para não bloquear exclusão do registro
        try {
            Files.deleteIfExists(diretorioBase().resolve(arquivo.getUrl()));
        } catch (IOException ignored) {}

        arquivoRepository.delete(arquivo);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // [M2] URL de download autenticada — caminho relativo no disco nunca exposto ao cliente
    private ArquivoAnamneseResponse toResponse(ArquivoAnamnese a) {
        UUID anamneseId = a.getAnamnese().getId();
        String downloadUrl = "/api/anamneses/" + anamneseId + "/arquivos/" + a.getId() + "/download";
        return new ArquivoAnamneseResponse(
            a.getId(), a.getTipo(), a.getNomeArquivo(),
            downloadUrl, a.getTamanhoBytes(), a.getCreatedAt()
        );
    }

    private Path diretorioBase() {
        return Paths.get(System.getProperty("user.dir"), "uploads", "anamneses");
    }
}
