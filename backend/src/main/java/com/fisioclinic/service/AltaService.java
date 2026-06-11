package com.fisioclinic.service;

import com.fisioclinic.dto.AltaDTO;
import com.fisioclinic.dto.AltaResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Alta;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.model.PlanoTratamento;
import com.fisioclinic.model.Sessao;
import com.fisioclinic.repository.AltaRepository;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import com.fisioclinic.repository.PlanoTratamentoRepository;
import com.fisioclinic.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AltaService — Regras de negócio do encerramento de tratamento (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Responsabilidades:
 *  - Validar que paciente, plano e fisioterapeuta existem antes de persistir
 *  - Impedir duplicata: uma alta por plano (quando plano_id é informado)
 *  - Calcular automaticamente num_sessoes_realizadas via contagem de sessões
 *    com status REALIZADO no repositório (M4/M5)
 *  - Atualizar plano_tratamento.status = "concluido" ao registrar alta com plano
 *  - Validar que o motivo está no domínio permitido
 *
 * Motivos válidos: alta_clinica | alta_administrativa | desistencia |
 *                  encaminhamento | obito
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AltaService {

    private static final List<String> MOTIVOS_VALIDOS = List.of(
        "alta_clinica", "alta_administrativa", "desistencia", "encaminhamento", "obito"
    );

    private final AltaRepository             altaRepository;
    private final PacienteRepository         pacienteRepository;
    private final PlanoTratamentoRepository  planoTratamentoRepository;
    private final FisioterapeutaRepository   fisioterapeutaRepository;
    private final SessaoRepository           sessaoRepository;
    // [Auditoria P2] Registro de evento CFM 1.821/07 ao registrar alta
    private final AuditoriaService           auditoriaService;

    // ── Registro de alta ─────────────────────────────────────────────────────

    public AltaResponse registrar(AltaDTO dto) {

        // Validar motivo antes de qualquer I/O
        if (!MOTIVOS_VALIDOS.contains(dto.motivo())) {
            throw new ConflictException("Motivo inválido. Valores aceitos: " + MOTIVOS_VALIDOS);
        }

        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Alta alta = new Alta();
        alta.setPaciente(paciente);
        alta.setDataAlta(dto.dataAlta() != null ? dto.dataAlta() : LocalDate.now());
        alta.setMotivo(dto.motivo());
        alta.setResultadoObjetivos(dto.resultadoObjetivos());
        alta.setOrientacoesDomiciliares(dto.orientacoesDomiciliares());
        alta.setRelatorioEvolucao(dto.relatorioEvolucao());
        alta.setRelatorioMedico(dto.relatorioMedico());
        alta.setAgendamentoRetorno(dto.agendamentoRetorno());
        alta.setSatisfacaoNota(dto.satisfacaoNota());
        alta.setSatisfacaoComentario(dto.satisfacaoComentario());

        // [M3] Vincular plano, verificar duplicata e marcar como concluído
        if (dto.planoId() != null) {
            PlanoTratamento plano = planoTratamentoRepository.findById(dto.planoId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano de tratamento não encontrado"));

            if (!plano.getPaciente().getId().equals(dto.pacienteId())) {
                throw new ResourceNotFoundException("Plano de tratamento não pertence a este paciente");
            }

            if (altaRepository.existsByPacienteIdAndPlanoTratamentoId(dto.pacienteId(), dto.planoId())) {
                throw new ConflictException("Já existe uma alta registrada para este plano de tratamento");
            }

            alta.setPlanoTratamento(plano);

            // [M3→M6] Marcar plano como concluído ao registrar a alta clínica
            plano.setStatus(com.fisioclinic.model.enums.StatusPlanoTratamento.CONCLUIDO);
            planoTratamentoRepository.save(plano);
        }

        // [M4] Vincular fisioterapeuta responsável pela alta
        if (dto.fisioterapeutaId() != null) {
            Fisioterapeuta fisio = fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado"));
            alta.setFisioterapeuta(fisio);
        }

        // [M4/M5] Calcular automaticamente o número de sessões realizadas via SessaoRepository
        int sessoesRealizadas = sessaoRepository.countByPacienteIdAndStatus(
            dto.pacienteId(), Sessao.StatusSessao.REALIZADO);
        alta.setNumSessoesRealizadas(sessoesRealizadas);

        Alta saved = altaRepository.save(alta);
        // [Auditoria CFM] Registra alta — metadados não-clínicos apenas (LGPD)
        UUID fisioAltaId = saved.getFisioterapeuta() != null ? saved.getFisioterapeuta().getId() : null;
        auditoriaService.registrar("ALTA", saved.getId(), saved.getPaciente().getId(),
            "CRIACAO", fisioAltaId, "{\"motivo\":\"" + saved.getMotivo() + "\"}");
        return toResponse(saved);
    }

    // ── Listagem por paciente ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AltaResponse> listarPorPaciente(UUID pacienteId) {
        return altaRepository.findByPacienteId(pacienteId).stream()
            .map(this::toResponse).toList();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AltaResponse buscarPorId(UUID id) {
        Alta alta = altaRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alta não encontrada"));
        return toResponse(alta);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private AltaResponse toResponse(Alta a) {
        AltaResponse.PlanoResumo planoResumo = a.getPlanoTratamento() != null
            ? new AltaResponse.PlanoResumo(
                a.getPlanoTratamento().getId(),
                a.getPlanoTratamento().getDiagnosticoCif(),
                a.getPlanoTratamento().getStatus().getValor())
            : null;

        AltaResponse.FisioterapeutaResumo fisioResumo = a.getFisioterapeuta() != null
            ? new AltaResponse.FisioterapeutaResumo(
                a.getFisioterapeuta().getId(),
                a.getFisioterapeuta().getNome(),
                a.getFisioterapeuta().getCrf())
            : null;

        return new AltaResponse(
            a.getId(),
            new AltaResponse.PacienteResumo(
                a.getPaciente().getId(),
                a.getPaciente().getNomeCompleto(),
                a.getPaciente().getCpf()),
            planoResumo,
            fisioResumo,
            a.getDataAlta(),
            a.getMotivo(),
            a.getResultadoObjetivos(),
            a.getOrientacoesDomiciliares(),
            a.getRelatorioEvolucao(),
            a.getRelatorioMedico(),
            a.getNumSessoesRealizadas(),
            a.getAgendamentoRetorno(),
            a.getSatisfacaoNota(),
            a.getSatisfacaoComentario(),
            a.getCreatedAt()
        );
    }
}
