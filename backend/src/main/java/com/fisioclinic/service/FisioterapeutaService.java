package com.fisioclinic.service;

import com.fisioclinic.dto.FisioterapeutaDTO;
import com.fisioclinic.dto.FisioterapeutaResponse;
import com.fisioclinic.dto.ResetSenhaAdminDTO;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.repository.FisioterapeutaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FisioterapeutaService — Gestão de profissionais e usuários do sistema
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Service (lógica de domínio)
 *
 * Responsabilidades:
 *  - Validar unicidade de e-mail e CRF antes de cadastrar
 *  - Normalizar dados: CRF em maiúsculas, e-mail em minúsculas, telefone só dígitos
 *  - Definir senha inicial "Fisio@123" quando o campo senha não é enviado
 *  - alterarStatus(): ativa ou desativa o acesso sem excluir o registro
 *    (fisioterapeuta inativo não consegue autenticar — verificado no AuthService)
 *
 * FisioterapeutaResponse nunca expõe o campo senhaHash.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FisioterapeutaService {

    private final FisioterapeutaRepository repository;
    private final PasswordEncoder passwordEncoder;

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FisioterapeutaResponse> listar() {
        return repository.findAll(PageRequest.of(0, 200, Sort.by("nome"))).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public FisioterapeutaResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public FisioterapeutaResponse criar(FisioterapeutaDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        if (repository.existsByCrf(dto.crf())) {
            throw new ConflictException("CRF já cadastrado");
        }

        Fisioterapeuta f = new Fisioterapeuta();
        f.setNome(dto.nome());
        f.setCrf(dto.crf().trim().toUpperCase());
        f.setEmail(dto.email().trim().toLowerCase());
        f.setTelefone(dto.telefone() != null ? dto.telefone().replaceAll("\\D", "") : null);
        f.setPerfil(dto.perfil() != null ? dto.perfil() : Fisioterapeuta.Perfil.FISIOTERAPEUTA);

        String senhaInicial = dto.senha() != null && !dto.senha().isBlank()
            ? dto.senha()
            : "Fisio@123";
        f.setSenhaHash(passwordEncoder.encode(senhaInicial));

        return toResponse(repository.save(f));
    }

    // ── Atualização parcial (PATCH) ──────────────────────────────────────────

    public FisioterapeutaResponse atualizar(UUID id, FisioterapeutaDTO dto) {
        Fisioterapeuta f = encontrarOuLancar(id);

        if (dto.nome()     != null) f.setNome(dto.nome());
        if (dto.crf()      != null) f.setCrf(dto.crf().trim().toUpperCase());
        if (dto.email()    != null) f.setEmail(dto.email().trim().toLowerCase());
        if (dto.telefone() != null) f.setTelefone(dto.telefone().replaceAll("\\D", ""));
        if (dto.perfil()   != null) f.setPerfil(dto.perfil());
        if (dto.ativo()    != null) f.setAtivo(dto.ativo());

        return toResponse(repository.save(f));
    }

    // ── Reset de senha por admin ─────────────────────────────────────────────

    public void resetarSenha(UUID id, ResetSenhaAdminDTO dto) {
        Fisioterapeuta f = encontrarOuLancar(id);
        f.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        repository.save(f);
    }

    // ── Status (ativar / desativar) ───────────────────────────────────────────

    public void alterarStatus(UUID id, boolean ativo) {
        Fisioterapeuta f = encontrarOuLancar(id);
        f.setAtivo(ativo);
        repository.save(f);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private FisioterapeutaResponse toResponse(Fisioterapeuta f) {
        return new FisioterapeutaResponse(
            f.getId(),
            f.getNome(),
            f.getCrf(),
            f.getEmail(),
            f.getTelefone(),
            f.getAtivo(),
            f.getPerfil(),
            f.getCreatedAt(),
            f.getUpdatedAt()
        );
    }

    private Fisioterapeuta encontrarOuLancar(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta não encontrado: " + id));
    }
}
