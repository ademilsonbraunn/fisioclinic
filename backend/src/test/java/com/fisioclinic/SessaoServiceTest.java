package com.fisioclinic;

import com.fisioclinic.dto.SessaoDTO;
import com.fisioclinic.dto.StatusDTO;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.model.Sala;
import com.fisioclinic.model.Sessao;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import com.fisioclinic.repository.PlanoTratamentoRepository;
import com.fisioclinic.repository.SalaRepository;
import com.fisioclinic.repository.SessaoRepository;
import com.fisioclinic.service.SessaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SessaoService — regras de negócio do agendamento.
 */
@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock private SessaoRepository sessaoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private FisioterapeutaRepository fisioterapeutaRepository;
    @Mock private SalaRepository salaRepository;
    @Mock private PlanoTratamentoRepository planoRepository;

    @InjectMocks
    private SessaoService sessaoService;

    private UUID pacienteId, fisioId, salaId;
    private Paciente paciente;
    private Fisioterapeuta fisio;
    private Sala sala;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        fisioId    = UUID.randomUUID();
        salaId     = UUID.randomUUID();

        paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setNomeCompleto("Ana Lima");
        paciente.setCpf("12345678901");

        fisio = new Fisioterapeuta();
        fisio.setId(fisioId);
        fisio.setNome("Dr. Carlos");
        fisio.setCrf("SP-12345");

        sala = new Sala();
        sala.setId(salaId);
        sala.setNome("Sala 1");
        sala.setTipo(Sala.TipoSala.BOX);
    }

    // SessaoDTO(pacienteId, fisioId, salaId, inicio, fim, tipo, status, obs, motivo, planoId)
    private SessaoDTO dtoBase() {
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 10, 9, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 6, 10, 10, 0);
        return new SessaoDTO(
            pacienteId, fisioId, salaId,
            inicio, fim,
            Sessao.TipoSessao.AVALIACAO,
            Sessao.StatusSessao.AGENDADO,
            null, null, null
        );
    }

    // ── Validação de intervalo ────────────────────────────────────────────────

    @Test
    @DisplayName("Criar sessão com fim <= início lança ConflictException")
    void criar_fimAnteriorInicio_lancaConflict() {
        SessaoDTO dto = new SessaoDTO(
            pacienteId, fisioId, salaId,
            LocalDateTime.of(2026, 6, 10, 10, 0),
            LocalDateTime.of(2026, 6, 10, 9, 0),
            Sessao.TipoSessao.AVALIACAO,
            Sessao.StatusSessao.AGENDADO,
            null, null, null
        );

        assertThatThrownBy(() -> sessaoService.criar(dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Hora fim deve ser posterior");
    }

    // ── Conflito de sala ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Criar sessão com sala ocupada lança ConflictException")
    void criar_salaOcupada_lancaConflict() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(fisioterapeutaRepository.findById(fisioId)).thenReturn(Optional.of(fisio));
        when(salaRepository.findById(salaId)).thenReturn(Optional.of(sala));

        Sessao sessaoExistente = new Sessao();
        sessaoExistente.setId(UUID.randomUUID());
        when(sessaoRepository.findConflitos(any(), any(), any(), any()))
            .thenReturn(List.of(sessaoExistente));

        assertThatThrownBy(() -> sessaoService.criar(dtoBase()))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Sala já ocupada");
    }

    @Test
    @DisplayName("Criar sessão sem conflito persiste com sucesso")
    void criar_semConflito_salva() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(fisioterapeutaRepository.findById(fisioId)).thenReturn(Optional.of(fisio));
        when(salaRepository.findById(salaId)).thenReturn(Optional.of(sala));
        when(sessaoRepository.findConflitos(any(), any(), any(), any())).thenReturn(List.of());

        Sessao sessaoSalva = new Sessao();
        sessaoSalva.setId(UUID.randomUUID());
        sessaoSalva.setPaciente(paciente);
        sessaoSalva.setFisioterapeuta(fisio);
        sessaoSalva.setSala(sala);
        sessaoSalva.setDataHoraInicio(LocalDateTime.of(2026, 6, 10, 9, 0));
        sessaoSalva.setDataHoraFim(LocalDateTime.of(2026, 6, 10, 10, 0));
        sessaoSalva.setTipoSessao(Sessao.TipoSessao.AVALIACAO);
        sessaoSalva.setStatus(Sessao.StatusSessao.AGENDADO);
        when(sessaoRepository.save(any())).thenReturn(sessaoSalva);

        assertThatCode(() -> sessaoService.criar(dtoBase()))
            .doesNotThrowAnyException();

        verify(sessaoRepository).save(any(Sessao.class));
    }

    // ── Atualização de status ─────────────────────────────────────────────────

    @Test
    @DisplayName("Cancelar sessão sem motivo lança ConflictException")
    void cancelar_semMotivo_lancaConflict() {
        UUID id = UUID.randomUUID();
        Sessao sessao = new Sessao();
        sessao.setId(id);
        sessao.setPaciente(paciente);
        sessao.setFisioterapeuta(fisio);
        sessao.setSala(sala);
        sessao.setStatus(Sessao.StatusSessao.AGENDADO);

        when(sessaoRepository.findById(id)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoService.atualizarStatus(id, new StatusDTO(Sessao.StatusSessao.CANCELADO, null)))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Motivo de cancelamento");
    }

    @Test
    @DisplayName("Cancelar sessão com motivo atualiza corretamente")
    void cancelar_comMotivo_atualizaStatus() {
        UUID id = UUID.randomUUID();
        Sessao sessao = new Sessao();
        sessao.setId(id);
        sessao.setPaciente(paciente);
        sessao.setFisioterapeuta(fisio);
        sessao.setSala(sala);
        sessao.setDataHoraInicio(LocalDateTime.of(2026, 6, 10, 9, 0));
        sessao.setDataHoraFim(LocalDateTime.of(2026, 6, 10, 10, 0));
        sessao.setStatus(Sessao.StatusSessao.AGENDADO);

        when(sessaoRepository.findById(id)).thenReturn(Optional.of(sessao));
        when(sessaoRepository.save(any())).thenReturn(sessao);

        assertThatCode(() ->
            sessaoService.atualizarStatus(id, new StatusDTO(Sessao.StatusSessao.CANCELADO, "Paciente solicitou"))
        ).doesNotThrowAnyException();

        verify(sessaoRepository).save(argThat(s ->
            s.getStatus() == Sessao.StatusSessao.CANCELADO
            && "Paciente solicitou".equals(s.getMotivoCancelamento())
        ));
    }
}
