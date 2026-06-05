package com.fisioclinic.service;

import com.fisioclinic.dto.EvolucaoDTO;
import com.fisioclinic.dto.EvolucaoResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Evolucao;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.PlanoTratamento;
import com.fisioclinic.model.Sessao;
import com.fisioclinic.repository.EvolucaoRepository;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PlanoTratamentoRepository;
import com.fisioclinic.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class EvolucaoService {

    private final EvolucaoRepository      evolucaoRepository;
    private final SessaoRepository        sessaoRepository;
    private final FisioterapeutaRepository fisioterapeutaRepository;
    private final PlanoTratamentoRepository planoTratamentoRepository;

    // ── Listagem por paciente ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EvolucaoResponse> listarPorPaciente(UUID pacienteId) {
        return evolucaoRepository.findByPacienteId(pacienteId).stream()
            .map(this::toResponse).toList();
    }

    // ── Busca por sessão ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EvolucaoResponse buscarPorSessao(UUID sessaoId) {
        Evolucao e = evolucaoRepository.findBySessaoId(sessaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Evolução não encontrada para esta sessão"));
        return toResponse(e);
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public EvolucaoResponse criar(EvolucaoDTO dto) {
        Sessao sessao = sessaoRepository.findById(dto.sessaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));

        if (evolucaoRepository.existsBySessaoId(dto.sessaoId())) {
            throw new ConflictException("Já existe uma evolução registrada para esta sessão");
        }

        Evolucao e = new Evolucao();
        e.setSessao(sessao);
        e.setPaciente(sessao.getPaciente());

        if (dto.fisioterapeutaId() != null) {
            Fisioterapeuta fisio = fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado"));
            e.setFisioterapeuta(fisio);
        }

        if (dto.planoTratamentoId() != null) {
            PlanoTratamento plano = planoTratamentoRepository.findById(dto.planoTratamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano de tratamento não encontrado"));
            e.setPlanoTratamento(plano);
        }

        e.setNumSessao(dto.numSessao());
        e.setDataHora(dto.dataHora() != null ? dto.dataHora() : LocalDateTime.now());
        e.setTempoAtendimentoMin(dto.tempoAtendimentoMin());
        e.setSubjetivo(dto.subjetivo());
        e.setObjetivo(dto.objetivo());
        e.setAvaliacao(dto.avaliacao());
        e.setPlanoEvolucao(dto.planoEvolucao());
        e.setTecnicasRealizadas(dto.tecnicasRealizadas());
        e.setAparelhos(dto.aparelhos());
        e.setEvaAntes(dto.evaAntes());
        e.setEvaDepois(dto.evaDepois());
        e.setCodigoTuss(dto.codigoTuss());
        e.setObservacoes(dto.observacoes());

        return toResponse(evolucaoRepository.save(e));
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private EvolucaoResponse toResponse(Evolucao e) {
        EvolucaoResponse.FisioterapeutaResumo fisioResumo = e.getFisioterapeuta() != null
            ? new EvolucaoResponse.FisioterapeutaResumo(
                e.getFisioterapeuta().getId(),
                e.getFisioterapeuta().getNome(),
                e.getFisioterapeuta().getCrf())
            : null;

        return new EvolucaoResponse(
            e.getId(),
            new EvolucaoResponse.SessaoResumo(
                e.getSessao().getId(),
                e.getSessao().getDataHoraInicio()),
            new EvolucaoResponse.PacienteResumo(
                e.getPaciente().getId(),
                e.getPaciente().getNomeCompleto()),
            fisioResumo,
            e.getPlanoTratamento() != null ? e.getPlanoTratamento().getId() : null,
            e.getNumSessao(),
            e.getDataHora(),
            e.getTempoAtendimentoMin(),
            e.getSubjetivo(),
            e.getObjetivo(),
            e.getAvaliacao(),
            e.getPlanoEvolucao(),
            e.getTecnicasRealizadas(),
            e.getAparelhos(),
            e.getEvaAntes(),
            e.getEvaDepois(),
            e.getCodigoTuss(),
            e.getObservacoes(),
            e.getCreatedAt()
        );
    }
}
