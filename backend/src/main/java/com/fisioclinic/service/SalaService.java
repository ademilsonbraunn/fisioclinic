package com.fisioclinic.service;

import com.fisioclinic.dto.SalaDTO;
import com.fisioclinic.dto.SalaResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Sala;
import com.fisioclinic.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SalaService — Gestão de salas e boxes de atendimento
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Responsabilidades:
 *  - Validar unicidade de nome de sala (case-insensitive)
 *  - Definir capacidade padrão = 1 quando não informada
 *  - Separar listagem completa (admin) de listagem de ativas (formulário de agendamento)
 *
 * Salas inativas continuam referenciadas em sessões históricas — nunca são excluídas,
 * apenas desativadas via PATCH ativo=false.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SalaService {

    private final SalaRepository repository;

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SalaResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SalaResponse> listarAtivas() {
        return repository.findByAtivoTrueOrderByNomeAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SalaResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public SalaResponse criar(SalaDTO dto) {
        if (repository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ConflictException("Já existe uma sala com esse nome");
        }

        Sala s = new Sala();
        s.setNome(dto.nome().trim());
        s.setTipo(dto.tipo());
        s.setCapacidade(dto.capacidade() != null ? dto.capacidade() : 1);

        return toResponse(repository.save(s));
    }

    // ── Atualização parcial (PATCH) ──────────────────────────────────────────

    public SalaResponse atualizar(UUID id, SalaDTO dto) {
        Sala s = encontrarOuLancar(id);

        if (dto.nome()       != null) s.setNome(dto.nome().trim());
        if (dto.tipo()       != null) s.setTipo(dto.tipo());
        if (dto.capacidade() != null) s.setCapacidade(dto.capacidade());
        if (dto.ativo()      != null) s.setAtivo(dto.ativo());

        return toResponse(repository.save(s));
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private SalaResponse toResponse(Sala s) {
        return new SalaResponse(
            s.getId(),
            s.getNome(),
            s.getTipo(),
            s.getCapacidade(),
            s.getAtivo()
        );
    }

    private Sala encontrarOuLancar(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada: " + id));
    }
}
