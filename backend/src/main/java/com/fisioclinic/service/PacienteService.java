package com.fisioclinic.service;

import com.fisioclinic.dto.PacienteDTO;
import com.fisioclinic.dto.PacienteResponse;
import com.fisioclinic.exception.ConflictException;
import com.fisioclinic.exception.ResourceNotFoundException;
import com.fisioclinic.model.ContatoEmergencia;
import com.fisioclinic.model.ConvenioPaciente;
import com.fisioclinic.model.Paciente;
import com.fisioclinic.repository.ContatoEmergenciaRepository;
import com.fisioclinic.repository.ConvenioPacienteRepository;
import com.fisioclinic.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ContatoEmergenciaRepository contatoEmergenciaRepository;
    private final ConvenioPacienteRepository convenioPacienteRepository;

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String busca) {
        List<Paciente> pacientes = (busca == null || busca.isBlank())
            ? pacienteRepository.findAll()
            : pacienteRepository.buscar(busca.trim());

        return pacientes.stream().map(this::toResponse).toList();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(UUID id) {
        return toResponse(encontrarOuLancar(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────

    public PacienteResponse criar(PacienteDTO dto) {
        String cpfLimpo = limparCPF(dto.cpf());

        if (pacienteRepository.existsByCpf(cpfLimpo)) {
            throw new ConflictException("CPF já cadastrado");
        }

        Paciente paciente = new Paciente();
        preencherPaciente(paciente, dto, cpfLimpo);
        paciente = pacienteRepository.save(paciente);

        salvarContatoEmergencia(paciente, dto);
        salvarConvenio(paciente, dto);

        return toResponse(paciente);
    }

    // ── Atualização parcial (PATCH) ──────────────────────────────────────────

    public PacienteResponse atualizar(UUID id, PacienteDTO dto) {
        Paciente paciente = encontrarOuLancar(id);

        if (dto.cpf() != null) {
            String cpfLimpo = limparCPF(dto.cpf());
            if (!cpfLimpo.equals(paciente.getCpf()) && pacienteRepository.existsByCpf(cpfLimpo)) {
                throw new ConflictException("CPF já pertence a outro paciente");
            }
            paciente.setCpf(cpfLimpo);
        }

        // Atualiza apenas os campos não-nulos recebidos
        if (dto.nomeCompleto()    != null) paciente.setNomeCompleto(dto.nomeCompleto());
        if (dto.dataNascimento()  != null) paciente.setDataNascimento(dto.dataNascimento());
        if (dto.sexo()            != null) paciente.setSexo(dto.sexo());
        if (dto.estadoCivil()     != null) paciente.setEstadoCivil(dto.estadoCivil());
        if (dto.profissao()       != null) paciente.setProfissao(dto.profissao());
        if (dto.email()           != null) paciente.setEmail(dto.email());
        if (dto.telefone()        != null) paciente.setTelefone(limparTelefone(dto.telefone()));
        if (dto.cep()             != null) paciente.setCep(dto.cep().replaceAll("\\D", ""));
        if (dto.logradouro()      != null) paciente.setLogradouro(dto.logradouro());
        if (dto.numero()          != null) paciente.setNumero(dto.numero());
        if (dto.complemento()     != null) paciente.setComplemento(dto.complemento());
        if (dto.bairro()          != null) paciente.setBairro(dto.bairro());
        if (dto.cidade()          != null) paciente.setCidade(dto.cidade());
        if (dto.uf()              != null) paciente.setUf(dto.uf());

        paciente = pacienteRepository.save(paciente);

        // Atualiza ou cria contato de emergência se algum campo foi enviado
        if (dto.emergenciaNome() != null || dto.emergenciaTelefone() != null) {
            salvarContatoEmergencia(paciente, dto);
        }

        // Atualiza convênio se tipoPagamento foi enviado
        if (dto.tipoPagamento() != null) {
            salvarConvenio(paciente, dto);
        }

        return toResponse(paciente);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void preencherPaciente(Paciente p, PacienteDTO dto, String cpfLimpo) {
        p.setNomeCompleto(dto.nomeCompleto());
        p.setCpf(cpfLimpo);
        p.setDataNascimento(dto.dataNascimento());
        p.setSexo(dto.sexo());
        p.setEstadoCivil(dto.estadoCivil());
        p.setProfissao(dto.profissao());
        p.setEmail(dto.email());
        p.setTelefone(limparTelefone(dto.telefone()));
        p.setCep(dto.cep() != null ? dto.cep().replaceAll("\\D", "") : null);
        p.setLogradouro(dto.logradouro());
        p.setNumero(dto.numero());
        p.setComplemento(dto.complemento());
        p.setBairro(dto.bairro());
        p.setCidade(dto.cidade());
        p.setUf(dto.uf());
    }

    private void salvarContatoEmergencia(Paciente paciente, PacienteDTO dto) {
        if (dto.emergenciaNome() == null && dto.emergenciaTelefone() == null) return;

        ContatoEmergencia contato = contatoEmergenciaRepository
            .findByPacienteId(paciente.getId())
            .orElseGet(() -> {
                ContatoEmergencia novo = new ContatoEmergencia();
                novo.setPaciente(paciente);
                return novo;
            });

        if (dto.emergenciaNome()       != null) contato.setNome(dto.emergenciaNome());
        if (dto.emergenciaParentesco() != null) contato.setParentesco(dto.emergenciaParentesco());
        if (dto.emergenciaTelefone()   != null) contato.setTelefone(limparTelefone(dto.emergenciaTelefone()));

        contatoEmergenciaRepository.save(contato);
    }

    private void salvarConvenio(Paciente paciente, PacienteDTO dto) {
        if (dto.tipoPagamento() == null) return;

        ConvenioPaciente convenio = convenioPacienteRepository
            .findFirstByPacienteIdOrderByIdAsc(paciente.getId())
            .orElseGet(() -> {
                ConvenioPaciente novo = new ConvenioPaciente();
                novo.setPaciente(paciente);
                return novo;
            });

        convenio.setTipoPagamento(dto.tipoPagamento());
        convenio.setNomeConvenio(dto.nomeConvenio());
        convenio.setNumCarteirinha(dto.numCarteirinha());
        convenio.setValidadePlano(dto.validadePlano());
        convenio.setResponsavelFinanceiro(dto.responsavelFinanceiro());

        convenioPacienteRepository.save(convenio);
    }

    private PacienteResponse toResponse(Paciente paciente) {
        ContatoEmergencia contato = contatoEmergenciaRepository
            .findByPacienteId(paciente.getId()).orElse(null);

        ConvenioPaciente convenio = convenioPacienteRepository
            .findFirstByPacienteIdOrderByIdAsc(paciente.getId()).orElse(null);

        return new PacienteResponse(
            paciente.getId(),
            paciente.getNomeCompleto(),
            paciente.getCpf(),
            paciente.getDataNascimento(),
            paciente.getSexo(),
            paciente.getEstadoCivil(),
            paciente.getProfissao(),
            paciente.getFotoUrl(),
            paciente.getEmail(),
            paciente.getTelefone(),
            paciente.getCep(),
            paciente.getLogradouro(),
            paciente.getNumero(),
            paciente.getComplemento(),
            paciente.getBairro(),
            paciente.getCidade(),
            paciente.getUf(),
            convenio != null ? convenio.getTipoPagamento()        : null,
            convenio != null ? convenio.getNomeConvenio()         : null,
            convenio != null ? convenio.getNumCarteirinha()       : null,
            convenio != null ? convenio.getValidadePlano()        : null,
            convenio != null ? convenio.getResponsavelFinanceiro(): null,
            contato  != null ? contato.getNome()                  : null,
            contato  != null ? contato.getParentesco()            : null,
            contato  != null ? contato.getTelefone()              : null,
            paciente.getCreatedAt(),
            paciente.getUpdatedAt()
        );
    }

    private Paciente encontrarOuLancar(UUID id) {
        return pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
    }

    private String limparCPF(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private String limparTelefone(String tel) {
        return tel == null ? null : tel.replaceAll("\\D", "");
    }
}
