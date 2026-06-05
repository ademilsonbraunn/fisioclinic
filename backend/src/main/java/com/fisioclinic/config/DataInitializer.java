package com.fisioclinic.config;

import com.fisioclinic.model.Fisioterapeuta;
import com.fisioclinic.repository.FisioterapeutaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FisioterapeutaRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!repository.existsByPerfil(Fisioterapeuta.Perfil.ADMIN)) {
            Fisioterapeuta admin = new Fisioterapeuta();
            admin.setNome("Administrador");
            admin.setCrf("ADMIN-0000");
            admin.setEmail("admin@fisioclinic.com");
            admin.setSenhaHash(passwordEncoder.encode("Admin@123"));
            admin.setPerfil(Fisioterapeuta.Perfil.ADMIN);
            admin.setAtivo(true);
            repository.save(admin);

            log.info("=================================================");
            log.info("  Admin padrão criado:                           ");
            log.info("  E-mail : admin@fisioclinic.com                 ");
            log.info("  Senha  : Admin@123                             ");
            log.info("  Troque a senha após o primeiro acesso!         ");
            log.info("=================================================");
        }
    }
}
