package com.fisioclinic.repository;

import com.fisioclinic.model.Fisioterapeuta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FisioterapeutaRepository — Repositório JPA para a entidade Fisioterapeuta
 * ─────────────────────────────────────────────────────────────────────────────
 * findByEmail(): usado pelo AuthService para autenticação — email é o login.
 * existsByPerfil(): verificação de segurança antes de remover o último ADMIN
 *   (impede que a clínica fique sem acesso administrativo).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface FisioterapeutaRepository extends JpaRepository<Fisioterapeuta, UUID> {

    List<Fisioterapeuta> findAllByOrderByNomeAsc();

    Optional<Fisioterapeuta> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCrf(String crf);

    boolean existsByPerfil(Fisioterapeuta.Perfil perfil);
}
