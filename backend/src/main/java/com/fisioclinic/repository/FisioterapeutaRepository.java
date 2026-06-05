package com.fisioclinic.repository;

import com.fisioclinic.model.Fisioterapeuta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FisioterapeutaRepository extends JpaRepository<Fisioterapeuta, UUID> {

    List<Fisioterapeuta> findAllByOrderByNomeAsc();

    Optional<Fisioterapeuta> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCrf(String crf);

    boolean existsByPerfil(Fisioterapeuta.Perfil perfil);
}
