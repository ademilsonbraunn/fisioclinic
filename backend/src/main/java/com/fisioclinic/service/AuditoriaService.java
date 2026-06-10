package com.fisioclinic.service;

import com.fisioclinic.dto.AuditoriaResponse;
import com.fisioclinic.model.AuditoriaProntuario;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.repository.AuditoriaRepository;
import com.fisioclinic.repository.FisioterapeutaRepository;
import com.fisioclinic.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AuditoriaService — Registro de eventos do prontuário (Auditoria — P2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Registra toda criação/alteração/assinatura em entidades clínicas para
 * conformidade com a Resolução CFM 1.821/07.
 *
 * [M2/M3/M5/M6] Chamado pelos services de Anamnese, PlanoTratamento,
 * Evolucao e Alta. Cada chamada ocorre dentro do @Transactional do service pai
 * (Propagation.REQUIRED). Falha silenciosa: um erro de auditoria nunca deve
 * impedir a operação clínica principal.
 *
 * LGPD art. 11: dados_novos contém apenas metadados (status, tipo) —
 * nunca dados clínicos sensíveis como texto de SOAP ou diagnóstico.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository      auditoriaRepository;
    private final PacienteRepository       pacienteRepository;
    private final FisioterapeutaRepository fisioterapeutaRepository;

    /**
     * Registra um evento de auditoria.
     * Chamado dentro do @Transactional do service pai (Propagation.REQUIRED).
     * dadosNovos: JSON string simples com metadados (ex: '{"status":"ativo"}') — nunca dados clínicos.
     * Falha silenciosa: nunca lança exceção para não bloquear a operação principal.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrar(String tipoEntidade, UUID entidadeId, UUID pacienteId,
                          String acao, UUID fisioterapeutaId, String dadosNovos) {
        try {
            Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
            if (paciente == null) return;

            Fisioterapeuta fisio = fisioterapeutaId != null
                ? fisioterapeutaRepository.findById(fisioterapeutaId).orElse(null)
                : null;

            auditoriaRepository.save(
                new AuditoriaProntuario(paciente, tipoEntidade, entidadeId, acao, fisio, dadosNovos)
            );
        } catch (Exception e) {
            // [Auditoria CFM] Falha silenciosa — não bloqueia a operação clínica
            // Logar apenas tipo de evento, nunca os dadosNovos (LGPD art. 11)
        }
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarPorPaciente(UUID pacienteId) {
        return auditoriaRepository.findByPacienteId(pacienteId)
            .stream().map(this::toResponse).toList();
    }

    private AuditoriaResponse toResponse(AuditoriaProntuario a) {
        return new AuditoriaResponse(
            a.getId(),
            a.getTipoEntidade(),
            a.getEntidadeId(),
            a.getAcao(),
            a.getFisioterapeuta() != null ? a.getFisioterapeuta().getNome() : null,
            a.getCreatedAt()
        );
    }
}
