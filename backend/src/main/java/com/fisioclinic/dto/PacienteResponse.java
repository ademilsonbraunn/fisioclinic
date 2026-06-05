package com.fisioclinic.dto;

import com.fisioclinic.model.ContatoEmergencia;
import com.fisioclinic.model.ConvenioPaciente;
import com.fisioclinic.model.Paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída — estrutura plana que espelha o que o frontend consome.
 * Combina Paciente + ContatoEmergencia + ConvenioPaciente em um único objeto JSON.
 */
public record PacienteResponse(

    UUID id,
    String nomeCompleto,
    String cpf,
    LocalDate dataNascimento,
    Paciente.Sexo sexo,
    Paciente.EstadoCivil estadoCivil,
    String profissao,
    String fotoUrl,
    String email,
    String telefone,

    // Endereço
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String uf,

    // Convênio
    ConvenioPaciente.TipoPagamento tipoPagamento,
    String nomeConvenio,
    String numCarteirinha,
    String validadePlano,
    String responsavelFinanceiro,

    // Contato de emergência
    String emergenciaNome,
    ContatoEmergencia.Parentesco emergenciaParentesco,
    String emergenciaTelefone,

    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}
