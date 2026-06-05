package com.fisioclinic.service;

import com.fisioclinic.dto.PlanoTratamentoDTO;
import com.fisioclinic.dto.PlanoTratamentoResponse;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Anamnese;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.model.PlanoTratamento;
import com.fisioclinic.repository.AnamneseRepository;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import com.fisioclinic.repository.PlanoTratamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanoTratamentoService {

    private static final List<String> STATUS_VALIDOS = List.of("ativo", "concluido", "cancelado");

    private final PlanoTratamentoRepository planoRepository;
    private final PacienteRepository pacienteRepository;
    private final AnamneseRepository anamneseRepository;
    private final FisioterapeutaRepository fisioterapeutaRepository;

    // ── Listagem por paciente ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PlanoTratamentoResponse> listarPorPaciente(UUID pacienteId) {
        pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return planoRepository.findByPacienteId(pacienteId).stream()
            .map(this::toResponse).toList();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PlanoTratamentoResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public PlanoTratamentoResponse criar(PlanoTratamentoDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Anamnese anamnese = null;
        if (dto.anamneseId() != null) {
            anamnese = anamneseRepository.findById(dto.anamneseId())
                .orElseThrow(() -> new ResourceNotFoundException("Anamnese não encontrada"));
        }

        Fisioterapeuta fisio = null;
        if (dto.fisioterapeutaId() != null) {
            fisio = fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado"));
        }

        PlanoTratamento plano = new PlanoTratamento();
        preencher(plano, dto, paciente, anamnese, fisio);
        return toResponse(planoRepository.save(plano));
    }

    // ── Atualização parcial (PATCH) ───────────────────────────────────────────

    public PlanoTratamentoResponse atualizar(UUID id, PlanoTratamentoDTO dto) {
        PlanoTratamento plano = encontrarOuLancar(id);

        if (dto.fisioterapeutaId() != null) {
            plano.setFisioterapeuta(fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado")));
        }
        if (dto.anamneseId() != null) {
            plano.setAnamnese(anamneseRepository.findById(dto.anamneseId())
                .orElseThrow(() -> new ResourceNotFoundException("Anamnese não encontrada")));
        }
        if (dto.diagnosticoCif()       != null) plano.setDiagnosticoCif(dto.diagnosticoCif());
        if (dto.cid10()                != null) plano.setCid10(dto.cid10());
        if (dto.objetivosCurtoPrazo()  != null) plano.setObjetivosCurtoPrazo(dto.objetivosCurtoPrazo());
        if (dto.objetivosLongoPrazo()  != null) plano.setObjetivosLongoPrazo(dto.objetivosLongoPrazo());
        if (dto.tecnicas()             != null) plano.setTecnicas(dto.tecnicas());
        if (dto.frequenciaSemanal()    != null) plano.setFrequenciaSemanal(dto.frequenciaSemanal());
        if (dto.numSessoesEstimado()   != null) plano.setNumSessoesEstimado(dto.numSessoesEstimado());
        if (dto.hipotesesTratamento()  != null) plano.setHipotesesTratamento(dto.hipotesesTratamento());
        if (dto.dataInicio()           != null) plano.setDataInicio(dto.dataInicio());
        if (dto.dataPrevisaoAlta()     != null) plano.setDataPrevisaoAlta(dto.dataPrevisaoAlta());
        if (dto.status()               != null) {
            if (!STATUS_VALIDOS.contains(dto.status())) {
                throw new IllegalArgumentException("Status inválido: " + dto.status());
            }
            plano.setStatus(dto.status());
        }

        return toResponse(planoRepository.save(plano));
    }

    // ── Atualização de status ─────────────────────────────────────────────────

    public PlanoTratamentoResponse atualizarStatus(UUID id, String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException("Status inválido. Use: ativo, concluido ou cancelado");
        }
        PlanoTratamento plano = encontrarOuLancar(id);
        plano.setStatus(status);
        return toResponse(planoRepository.save(plano));
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void preencher(PlanoTratamento p, PlanoTratamentoDTO dto,
                           Paciente paciente, Anamnese anamnese, Fisioterapeuta fisio) {
        p.setPaciente(paciente);
        p.setAnamnese(anamnese);
        p.setFisioterapeuta(fisio);
        p.setDiagnosticoCif(dto.diagnosticoCif());
        p.setCid10(dto.cid10());
        p.setObjetivosCurtoPrazo(dto.objetivosCurtoPrazo());
        p.setObjetivosLongoPrazo(dto.objetivosLongoPrazo());
        p.setTecnicas(dto.tecnicas());
        p.setFrequenciaSemanal(dto.frequenciaSemanal());
        p.setNumSessoesEstimado(dto.numSessoesEstimado());
        p.setHipotesesTratamento(dto.hipotesesTratamento());
        p.setDataInicio(dto.dataInicio() != null ? dto.dataInicio() : LocalDate.now());
        p.setDataPrevisaoAlta(dto.dataPrevisaoAlta());
        p.setStatus(dto.status() != null && STATUS_VALIDOS.contains(dto.status()) ? dto.status() : "ativo");
    }

    private PlanoTratamentoResponse toResponse(PlanoTratamento p) {
        PlanoTratamentoResponse.FisioterapeutaResumo fisioResumo = p.getFisioterapeuta() != null
            ? new PlanoTratamentoResponse.FisioterapeutaResumo(
                p.getFisioterapeuta().getId(),
                p.getFisioterapeuta().getNome(),
                p.getFisioterapeuta().getCrf())
            : null;

        PlanoTratamentoResponse.AnamneseResumo anamneseResumo = p.getAnamnese() != null
            ? new PlanoTratamentoResponse.AnamneseResumo(
                p.getAnamnese().getId(),
                p.getAnamnese().getDataAvaliacao(),
                p.getAnamnese().getQueixaPrincipal())
            : null;

        return new PlanoTratamentoResponse(
            p.getId(),
            new PlanoTratamentoResponse.PacienteResumo(
                p.getPaciente().getId(),
                p.getPaciente().getNomeCompleto(),
                p.getPaciente().getCpf()),
            anamneseResumo,
            fisioResumo,
            p.getDiagnosticoCif(),
            p.getCid10(),
            p.getObjetivosCurtoPrazo(),
            p.getObjetivosLongoPrazo(),
            p.getTecnicas(),
            p.getFrequenciaSemanal(),
            p.getNumSessoesEstimado(),
            p.getHipotesesTratamento(),
            p.getDataInicio(),
            p.getDataPrevisaoAlta(),
            p.getStatus(),
            p.getCreatedAt()
        );
    }

    private PlanoTratamento encontrarOuLancar(UUID id) {
        return planoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plano de tratamento não encontrado: " + id));
    }
}
