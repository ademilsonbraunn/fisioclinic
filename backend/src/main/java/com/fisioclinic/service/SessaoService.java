package com.fisioclinic.service;

import com.fisioclinic.dto.SessaoDTO;
import com.fisioclinic.dto.SessaoResponse;
import com.fisioclinic.dto.StatusDTO;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.model.Sala;
import com.fisioclinic.model.Sessao;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import com.fisioclinic.repository.SalaRepository;
import com.fisioclinic.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SessaoService — Regras de negócio do agendamento (Módulo 4)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Responsabilidades principais:
 *  - Criar e atualizar sessões validando:
 *      1. Intervalo de horário (fim > início)
 *      2. Existência de paciente, fisioterapeuta e sala
 *      3. Conflito de sala: duas sessões ativas não podem ocupar a mesma sala
 *         no mesmo horário (sessões CANCELADO/FALTOU são ignoradas na checagem)
 *  - listarSemana(): calcula automaticamente seg–dom da semana corrente
 *  - atualizarStatus(): exige motivo_cancelamento quando status = CANCELADO
 *  - calcularDuracao(): persiste duração em minutos para facilitar relatórios
 *
 * STATUS_SEM_CONFLITO define quais status liberam o slot de sala novamente.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SessaoService {

    // Sessões nesses status não travam a sala — slot considerado livre
    private static final List<Sessao.StatusSessao> STATUS_SEM_CONFLITO = List.of(
        Sessao.StatusSessao.CANCELADO,
        Sessao.StatusSessao.FALTOU
    );

    private final SessaoRepository      sessaoRepository;
    private final PacienteRepository    pacienteRepository;
    private final FisioterapeutaRepository fisioterapeutaRepository;
    private final SalaRepository        salaRepository;

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SessaoResponse> listar(LocalDate dataInicio, LocalDate dataFim, UUID pacienteId) {
        if (pacienteId != null) return listarPorPaciente(pacienteId);
        if (dataInicio == null || dataFim == null) return listarSemana();
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim    = dataFim.plusDays(1).atStartOfDay();
        return sessaoRepository.findByPeriodo(inicio, fim).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SessaoResponse> listarPorPaciente(UUID pacienteId) {
        pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return sessaoRepository.findByPacienteId(pacienteId).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SessaoResponse> listarSemana() {
        LocalDate hoje       = LocalDate.now();
        LocalDate segundaFeira = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime inicio = segundaFeira.atStartOfDay();
        LocalDateTime fim    = segundaFeira.plusDays(7).atStartOfDay();
        return sessaoRepository.findByPeriodo(inicio, fim).stream()
            .map(this::toResponse).toList();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SessaoResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public SessaoResponse criar(SessaoDTO dto) {
        validarIntervalo(dto.dataHoraInicio(), dto.dataHoraFim());

        Paciente       paciente       = pacienteRepository.findById(dto.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        Fisioterapeuta fisioterapeuta = fisioterapeutaRepository.findById(dto.fisioterapeutaId())
            .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado"));
        Sala sala = salaRepository.findById(dto.salaId())
            .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada"));

        verificarConflito(dto.salaId(), dto.dataHoraInicio(), dto.dataHoraFim(), null);

        Sessao sessao = new Sessao();
        preencherSessao(sessao, dto, paciente, fisioterapeuta, sala);
        return toResponse(sessaoRepository.save(sessao));
    }

    // ── Atualização completa (PATCH) ─────────────────────────────────────────

    public SessaoResponse atualizar(UUID id, SessaoDTO dto) {
        Sessao sessao = encontrarOuLancar(id);

        if (dto.dataHoraInicio() != null && dto.dataHoraFim() != null) {
            validarIntervalo(dto.dataHoraInicio(), dto.dataHoraFim());
            verificarConflito(
                dto.salaId() != null ? dto.salaId() : sessao.getSala().getId(),
                dto.dataHoraInicio(),
                dto.dataHoraFim(),
                id
            );
        }

        if (dto.pacienteId() != null) {
            sessao.setPaciente(pacienteRepository.findById(dto.pacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado")));
        }
        if (dto.fisioterapeutaId() != null) {
            sessao.setFisioterapeuta(fisioterapeutaRepository.findById(dto.fisioterapeutaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado")));
        }
        if (dto.salaId() != null) {
            sessao.setSala(salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada")));
        }
        if (dto.dataHoraInicio()    != null) sessao.setDataHoraInicio(dto.dataHoraInicio());
        if (dto.dataHoraFim()       != null) sessao.setDataHoraFim(dto.dataHoraFim());
        if (dto.tipoSessao()        != null) sessao.setTipoSessao(dto.tipoSessao());
        if (dto.status()            != null) sessao.setStatus(dto.status());
        if (dto.observacoes()       != null) sessao.setObservacoes(dto.observacoes());
        if (dto.motivoCancelamento()!= null) sessao.setMotivoCancelamento(dto.motivoCancelamento());

        sessao.setDuracaoMinutos(calcularDuracao(sessao.getDataHoraInicio(), sessao.getDataHoraFim()));

        return toResponse(sessaoRepository.save(sessao));
    }

    // ── Atualização de status ────────────────────────────────────────────────

    public SessaoResponse atualizarStatus(UUID id, StatusDTO dto) {
        Sessao sessao = encontrarOuLancar(id);

        if (dto.status() == Sessao.StatusSessao.CANCELADO
                && (dto.motivoCancelamento() == null || dto.motivoCancelamento().isBlank())) {
            throw new ConflictException("Motivo de cancelamento é obrigatório");
        }

        sessao.setStatus(dto.status());
        if (dto.motivoCancelamento() != null) {
            sessao.setMotivoCancelamento(dto.motivoCancelamento());
        }

        return toResponse(sessaoRepository.save(sessao));
    }

    // ── Exclusão ─────────────────────────────────────────────────────────────

    public void excluir(UUID id) {
        Sessao sessao = encontrarOuLancar(id);
        sessaoRepository.delete(sessao);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void preencherSessao(Sessao s, SessaoDTO dto,
                                  Paciente paciente, Fisioterapeuta fisio, Sala sala) {
        s.setPaciente(paciente);
        s.setFisioterapeuta(fisio);
        s.setSala(sala);
        s.setDataHoraInicio(dto.dataHoraInicio());
        s.setDataHoraFim(dto.dataHoraFim());
        s.setTipoSessao(dto.tipoSessao());
        s.setStatus(dto.status());
        s.setObservacoes(dto.observacoes());
        s.setMotivoCancelamento(dto.motivoCancelamento());
        s.setDuracaoMinutos(calcularDuracao(dto.dataHoraInicio(), dto.dataHoraFim()));
    }

    private void verificarConflito(UUID salaId, LocalDateTime inicio,
                                    LocalDateTime fim, UUID excludeId) {
        List<Sessao> conflitos = (excludeId == null)
            ? sessaoRepository.findConflitos(salaId, inicio, fim, STATUS_SEM_CONFLITO)
            : sessaoRepository.findConflitosComExclusao(salaId, inicio, fim, excludeId, STATUS_SEM_CONFLITO);

        if (!conflitos.isEmpty()) {
            throw new ConflictException("Sala já ocupada nesse horário");
        }
    }

    private void validarIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        if (fim != null && inicio != null && !fim.isAfter(inicio)) {
            throw new ConflictException("Hora fim deve ser posterior à hora início");
        }
    }

    private int calcularDuracao(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) return 0;
        return (int) ChronoUnit.MINUTES.between(inicio, fim);
    }

    private SessaoResponse toResponse(Sessao s) {
        SessaoResponse.SalaResumo salaResumo = s.getSala() != null
            ? new SessaoResponse.SalaResumo(
                s.getSala().getId(),
                s.getSala().getNome(),
                s.getSala().getTipo().name())
            : null;

        return new SessaoResponse(
            s.getId(),
            new SessaoResponse.PacienteResumo(
                s.getPaciente().getId(),
                s.getPaciente().getNomeCompleto(),
                s.getPaciente().getCpf()
            ),
            new SessaoResponse.FisioterapeutaResumo(
                s.getFisioterapeuta().getId(),
                s.getFisioterapeuta().getNome(),
                s.getFisioterapeuta().getCrf()
            ),
            salaResumo,
            s.getDataHoraInicio(),
            s.getDataHoraFim(),
            s.getTipoSessao(),
            s.getStatus(),
            s.getObservacoes(),
            s.getMotivoCancelamento(),
            s.getCreatedAt()
        );
    }

    private Sessao encontrarOuLancar(UUID id) {
        return sessaoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada: " + id));
    }
}
