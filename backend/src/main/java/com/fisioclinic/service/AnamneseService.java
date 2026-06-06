package com.fisioclinic.service;

import com.fisioclinic.dto.AnamneseDTO;
import com.fisioclinic.dto.AnamneseResponse;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Anamnese;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.repository.AnamneseRepository;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AnamneseService — Regras de negócio da anamnese (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Responsabilidades:
 *  - Criar e atualizar anamneses vinculando paciente e opcionalmente fisioterapeuta
 *  - O campo avaliacaoFisica (Map<String,Object>) é salvo como JSONB no PostgreSQL
 *    e pode conter postura, ADM, força muscular, EVA, goniometria e testes especiais
 *    sem necessidade de alterar o schema do banco
 *  - fisioterapeutaId é opcional — anamnese pode ser registrada sem vincular o profissional
 *
 * Um paciente pode ter múltiplas anamneses ao longo do tratamento (reavaliações).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;
    private final PacienteRepository pacienteRepository;
    private final FisioterapeutaRepository fisioterapeutaRepository;

    // ── Listagem por paciente ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AnamneseResponse> listarPorPaciente(UUID pacienteId) {
        pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return anamneseRepository.findByPacienteId(pacienteId).stream()
            .map(this::toResponse).toList();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnamneseResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public AnamneseResponse criar(AnamneseDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Fisioterapeuta fisio = null;
        if (dto.fisioterapeutaId() != null) {
            fisio = fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado"));
        }

        Anamnese anamnese = new Anamnese();
        preencher(anamnese, dto, paciente, fisio);
        return toResponse(anamneseRepository.save(anamnese));
    }

    // ── Atualização parcial (PATCH) ───────────────────────────────────────────

    public AnamneseResponse atualizar(UUID id, AnamneseDTO dto) {
        Anamnese anamnese = encontrarOuLancar(id);

        if (dto.fisioterapeutaId() != null) {
            anamnese.setFisioterapeuta(fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado")));
        }
        if (dto.queixaPrincipal()     != null) anamnese.setQueixaPrincipal(dto.queixaPrincipal());
        if (dto.historicoDoencaAtual() != null) anamnese.setHistoricoDoencaAtual(dto.historicoDoencaAtual());
        if (dto.tempoInicioSintomas()  != null) anamnese.setTempoInicioSintomas(dto.tempoInicioSintomas());
        if (dto.doencasPreexistentes() != null) anamnese.setDoencasPreexistentes(dto.doencasPreexistentes());
        if (dto.cirurgiasAnteriores()  != null) anamnese.setCirurgiasAnteriores(dto.cirurgiasAnteriores());
        if (dto.medicamentos()         != null) anamnese.setMedicamentos(dto.medicamentos());
        if (dto.alergias()             != null) anamnese.setAlergias(dto.alergias());
        if (dto.historicoFamiliar()    != null) anamnese.setHistoricoFamiliar(dto.historicoFamiliar());
        if (dto.observacoes()          != null) anamnese.setObservacoes(dto.observacoes());
        if (dto.avaliacaoFisica()      != null) anamnese.setAvaliacaoFisica(dto.avaliacaoFisica());

        return toResponse(anamneseRepository.save(anamnese));
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void preencher(Anamnese a, AnamneseDTO dto, Paciente paciente, Fisioterapeuta fisio) {
        a.setPaciente(paciente);
        a.setFisioterapeuta(fisio);
        a.setQueixaPrincipal(dto.queixaPrincipal());
        a.setHistoricoDoencaAtual(dto.historicoDoencaAtual());
        a.setTempoInicioSintomas(dto.tempoInicioSintomas());
        a.setDoencasPreexistentes(dto.doencasPreexistentes());
        a.setCirurgiasAnteriores(dto.cirurgiasAnteriores());
        a.setMedicamentos(dto.medicamentos());
        a.setAlergias(dto.alergias());
        a.setHistoricoFamiliar(dto.historicoFamiliar());
        a.setObservacoes(dto.observacoes());
        a.setAvaliacaoFisica(dto.avaliacaoFisica());
    }

    private AnamneseResponse toResponse(Anamnese a) {
        AnamneseResponse.FisioterapeutaResumo fisioResumo = a.getFisioterapeuta() != null
            ? new AnamneseResponse.FisioterapeutaResumo(
                a.getFisioterapeuta().getId(),
                a.getFisioterapeuta().getNome(),
                a.getFisioterapeuta().getCrf())
            : null;

        return new AnamneseResponse(
            a.getId(),
            new AnamneseResponse.PacienteResumo(
                a.getPaciente().getId(),
                a.getPaciente().getNomeCompleto(),
                a.getPaciente().getCpf()),
            fisioResumo,
            a.getDataAvaliacao(),
            a.getQueixaPrincipal(),
            a.getHistoricoDoencaAtual(),
            a.getTempoInicioSintomas(),
            a.getDoencasPreexistentes(),
            a.getCirurgiasAnteriores(),
            a.getMedicamentos(),
            a.getAlergias(),
            a.getHistoricoFamiliar(),
            a.getObservacoes(),
            a.getAvaliacaoFisica(),
            a.getCreatedAt()
        );
    }

    private Anamnese encontrarOuLancar(UUID id) {
        return anamneseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anamnese não encontrada: " + id));
    }
}
