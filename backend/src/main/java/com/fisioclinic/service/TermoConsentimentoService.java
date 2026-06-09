package com.fisioclinic.service;

import com.fisioclinic.dto.TermoConsentimentoDTO;
import com.fisioclinic.dto.TermoConsentimentoResponse;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.model.PlanoTratamento;
import com.fisioclinic.model.TermoConsentimento;
import com.fisioclinic.repository.PacienteRepository;
import com.fisioclinic.repository.PlanoTratamentoRepository;
import com.fisioclinic.repository.TermoConsentimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimentoService — Regras de negócio do TCLE (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Registra o Termo de Consentimento Livre e Esclarecido com o timestamp
 * exato da assinatura, conforme exige a Resolução CFM 1.821/07.
 *
 * O tipo padrão é 'tcle'; outros valores válidos: autorizacao | outro
 * (espelha o CHECK da tabela termos_consentimento no schema.sql).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TermoConsentimentoService {

    private final TermoConsentimentoRepository termoRepository;
    private final PacienteRepository           pacienteRepository;
    private final PlanoTratamentoRepository    planoRepository;

    // ── Registro ─────────────────────────────────────────────────────────────

    public TermoConsentimentoResponse registrar(TermoConsentimentoDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        PlanoTratamento plano = null;
        if (dto.planoId() != null) {
            plano = planoRepository.findById(dto.planoId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano de tratamento não encontrado"));
        }

        TermoConsentimento termo = new TermoConsentimento();
        termo.setPaciente(paciente);
        termo.setPlano(plano);
        termo.setTipo(dto.tipo() != null && !dto.tipo().isBlank() ? dto.tipo() : "tcle");
        termo.setConteudo(dto.conteudo());
        termo.setAssinadoEm(dto.assinadoEm());

        return toResponse(termoRepository.save(termo));
    }

    // ── Listagem por plano ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TermoConsentimentoResponse> listarPorPlano(UUID planoId) {
        planoRepository.findById(planoId)
            .orElseThrow(() -> new ResourceNotFoundException("Plano de tratamento não encontrado"));
        return termoRepository.findByPlanoId(planoId)
            .stream().map(this::toResponse).toList();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private TermoConsentimentoResponse toResponse(TermoConsentimento t) {
        TermoConsentimentoResponse.PlanoResumo planoResumo = t.getPlano() != null
            ? new TermoConsentimentoResponse.PlanoResumo(
                t.getPlano().getId(),
                t.getPlano().getDiagnosticoCif())
            : null;

        return new TermoConsentimentoResponse(
            t.getId(), t.getTipo(), t.getAssinadoEm(), t.getCreatedAt(), planoResumo
        );
    }
}
