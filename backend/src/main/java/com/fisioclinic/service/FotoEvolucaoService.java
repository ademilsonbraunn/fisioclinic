package com.fisioclinic.service;

import com.fisioclinic.dto.FotoEvolucaoResponse;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Evolucao;
import com.fisioclinic.model.FotoEvolucao;
import com.fisioclinic.repository.EvolucaoRepository;
import com.fisioclinic.repository.FotoEvolucaoRepository;
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
 * FotoEvolucaoService — Upload e gestão de fotos comparativas (Módulo 5 — P2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * [M5] Fotos vinculadas a uma evolução clínica. Padrão idêntico ao
 * ArquivoAnamneseService (M2): armazenamento em disco, URL autenticada.
 *
 * Arquivos físicos ficam em: {raiz-da-app}/uploads/evolucoes/{evolucaoId}/
 * O campo 'url' na entidade armazena o caminho relativo no disco.
 *
 * Limite por arquivo: 20 MB. Tipos válidos: antes | depois | comparativo | outro.
 * Apenas imagens são aceitas (image/*).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FotoEvolucaoService {

    private static final List<String> TIPOS_VALIDOS =
        List.of("antes", "depois", "comparativo", "outro");

    private static final long MAX_BYTES = 20L * 1024 * 1024; // 20 MB

    private final FotoEvolucaoRepository fotoRepository;
    private final EvolucaoRepository     evolucaoRepository;

    // ── Upload ───────────────────────────────────────────────────────────────

    public FotoEvolucaoResponse salvar(UUID evolucaoId, MultipartFile file, String tipo) {
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException(
                "Tipo inválido. Use: antes, depois, comparativo ou outro");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o limite de 20 MB");
        }
        // [M5] Valida que é uma imagem — Content-Type deve começar com image/
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são aceitas (jpeg, png, webp, etc.)");
        }

        Evolucao evolucao = evolucaoRepository.findById(evolucaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Evolução não encontrada: " + evolucaoId));

        try {
            Path dir = diretorioBase().resolve(evolucaoId.toString());
            Files.createDirectories(dir);

            // Nome único no disco para evitar sobrescrita
            String nomeOriginal = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "foto";
            String nomeSanitizado = UUID.randomUUID() + "_" +
                nomeOriginal.replaceAll("[^a-zA-Z0-9._\\-]", "_");
            file.transferTo(dir.resolve(nomeSanitizado));

            // Caminho relativo no disco: {evolucaoId}/{nomeSanitizado}
            String caminhoRelativo = evolucaoId + "/" + nomeSanitizado;

            FotoEvolucao foto = new FotoEvolucao();
            foto.setEvolucao(evolucao);
            foto.setTipo(tipo);
            foto.setNomeArquivo(nomeOriginal);
            foto.setUrl(caminhoRelativo); // disco — convertido para URL de download em toResponse
            foto.setTamanhoBytes(file.getSize());

            return toResponse(fotoRepository.save(foto));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar foto: " + e.getMessage(), e);
        }
    }

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FotoEvolucaoResponse> listar(UUID evolucaoId) {
        evolucaoRepository.findById(evolucaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Evolução não encontrada: " + evolucaoId));
        return fotoRepository.findByEvolucaoIdOrderByCreatedAtAsc(evolucaoId)
            .stream().map(this::toResponse).toList();
    }

    // ── Download ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] download(UUID fotoId) {
        FotoEvolucao foto = fotoRepository.findById(fotoId)
            .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada: " + fotoId));

        Path caminho = diretorioBase().resolve(foto.getUrl());
        if (!Files.exists(caminho)) {
            throw new ResourceNotFoundException("Arquivo físico não encontrado no servidor");
        }
        try {
            return Files.readAllBytes(caminho);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler foto: " + e.getMessage(), e);
        }
    }

    // ── Deleção ──────────────────────────────────────────────────────────────

    public void deletar(UUID fotoId) {
        FotoEvolucao foto = fotoRepository.findById(fotoId)
            .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada: " + fotoId));

        // Remove arquivo físico — silencioso para não bloquear exclusão do registro
        try {
            Files.deleteIfExists(diretorioBase().resolve(foto.getUrl()));
        } catch (IOException ignored) {}

        fotoRepository.delete(foto);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // [M5 P2] URL de download autenticada — caminho relativo no disco nunca exposto ao cliente (LGPD)
    private FotoEvolucaoResponse toResponse(FotoEvolucao f) {
        UUID evolucaoId = f.getEvolucao().getId();
        String downloadUrl = "/api/evolucoes/" + evolucaoId + "/fotos/" + f.getId() + "/arquivo";
        return new FotoEvolucaoResponse(
            f.getId(), evolucaoId, f.getTipo(), f.getNomeArquivo(),
            downloadUrl, f.getTamanhoBytes(), f.getDescricao(), f.getCreatedAt()
        );
    }

    private Path diretorioBase() {
        return Paths.get(System.getProperty("user.dir"), "uploads", "evolucoes");
    }
}
