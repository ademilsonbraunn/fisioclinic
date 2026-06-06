package com.fisioclinic.repository;

import com.fisioclinic.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SessaoRepository — Repositório JPA para a entidade Sessao
 * ─────────────────────────────────────────────────────────────────────────────
 * Todas as queries usam JOIN FETCH para evitar o problema N+1: paciente,
 * fisioterapeuta e sala são carregados em uma única query SQL.
 *
 * findByPeriodo(): usada pela agenda semanal — retorna todas as sessões
 *   dentro de um intervalo [inicio, fim).
 * findByPacienteId(): histórico de sessões de um paciente, ordem desc.
 * findConflitos(): verifica sobreposição de horário na mesma sala — lógica
 *   de intervalo: A e B se sobrepõem quando A.inicio < B.fim && B.inicio < A.fim.
 *   Exclui status FALTOU e CANCELADO pois não ocupam fisicamente a sala.
 * findConflitosComExclusao(): variante usada ao editar sessão existente
 *   (exclui a própria sessão da verificação de conflito).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Query("""
        SELECT s FROM Sessao s
        JOIN FETCH s.paciente
        JOIN FETCH s.fisioterapeuta
        LEFT JOIN FETCH s.sala
        WHERE s.dataHoraInicio >= :inicio
          AND s.dataHoraInicio <  :fim
        ORDER BY s.dataHoraInicio
    """)
    List<Sessao> findByPeriodo(@Param("inicio") LocalDateTime inicio,
                               @Param("fim")    LocalDateTime fim);

    @Query("""
        SELECT s FROM Sessao s
        JOIN FETCH s.paciente
        JOIN FETCH s.fisioterapeuta
        LEFT JOIN FETCH s.sala
        WHERE s.paciente.id = :pacienteId
        ORDER BY s.dataHoraInicio DESC
    """)
    List<Sessao> findByPacienteId(@Param("pacienteId") UUID pacienteId);

    @Query("""
        SELECT s FROM Sessao s
        WHERE s.sala.id = :salaId
          AND s.status NOT IN :statusExcluidos
          AND :inicio < s.dataHoraFim
          AND :fim    > s.dataHoraInicio
    """)
    List<Sessao> findConflitos(@Param("salaId")          UUID salaId,
                               @Param("inicio")          LocalDateTime inicio,
                               @Param("fim")             LocalDateTime fim,
                               @Param("statusExcluidos") List<Sessao.StatusSessao> statusExcluidos);

    @Query("""
        SELECT s FROM Sessao s
        WHERE s.sala.id = :salaId
          AND s.status NOT IN :statusExcluidos
          AND :inicio < s.dataHoraFim
          AND :fim    > s.dataHoraInicio
          AND s.id <> :excludeId
    """)
    List<Sessao> findConflitosComExclusao(@Param("salaId")          UUID salaId,
                                          @Param("inicio")          LocalDateTime inicio,
                                          @Param("fim")             LocalDateTime fim,
                                          @Param("excludeId")       UUID excludeId,
                                          @Param("statusExcluidos") List<Sessao.StatusSessao> statusExcluidos);

    // [M6] Conta sessões realizadas de um paciente — usado pelo AltaService para
    // calcular num_sessoes_realizadas ao registrar a alta
    int countByPacienteIdAndStatus(UUID pacienteId, Sessao.StatusSessao status);
}
