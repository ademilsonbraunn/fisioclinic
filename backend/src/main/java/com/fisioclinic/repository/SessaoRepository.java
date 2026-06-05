package com.fisioclinic.repository;

import com.fisioclinic.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Query("""
        SELECT s FROM Sessao s
        JOIN FETCH s.paciente
        JOIN FETCH s.fisioterapeuta
        JOIN FETCH s.sala
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
        JOIN FETCH s.sala
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
}
