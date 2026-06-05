package com.fisioclinic.dto;

import com.fisioclinic.model.ContatoEmergencia;
import com.fisioclinic.model.ConvenioPaciente;
import com.fisioclinic.model.Paciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de entrada para criação (POST) e atualização parcial (PATCH) de paciente.
 * Para POST, use @Valid no controller — os campos marcados com @Not* serão validados.
 * Para PATCH, não use @Valid — campos nulos significam "manter valor atual".
 */
public record PacienteDTO(

    // Dados pessoais obrigatórios
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve conter 11 dígitos")
    String cpf,

    @NotNull(message = "Data de nascimento é obrigatória")
    LocalDate dataNascimento,

    @NotNull(message = "Sexo é obrigatório")
    Paciente.Sexo sexo,

    // Dados pessoais opcionais
    Paciente.EstadoCivil estadoCivil,
    String profissao,
    String fotoBase64,

    // Contato obrigatório
    @NotBlank(message = "Telefone é obrigatório")
    String telefone,

    // Contato opcional
    String email,

    // Endereço (tudo opcional)
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String uf,

    // Convênio / pagamento
    ConvenioPaciente.TipoPagamento tipoPagamento,
    String nomeConvenio,
    String numCarteirinha,
    String validadePlano,
    String responsavelFinanceiro,

    // Contato de emergência (todos opcionais)
    String emergenciaNome,
    ContatoEmergencia.Parentesco emergenciaParentesco,
    String emergenciaTelefone

) {}
