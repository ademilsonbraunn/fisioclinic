package com.fisioclinic.dto;

import com.fisioclinic.model.ContatoEmergencia;
import com.fisioclinic.model.ConvenioPaciente;
import com.fisioclinic.model.Paciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PacienteDTO — Entrada para criação (POST) e atualização parcial (PATCH)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 * Módulo: 1 — Cadastro do paciente
 *
 * Agrega em um único record os dados do paciente, convênio e contato de
 * emergência — evitando três requisições separadas no formulário de cadastro.
 *
 * POST:  usar @Valid no controller — campos marcados com @Not* são validados.
 * PATCH: não usar @Valid — campos nulos significam "manter valor atual".
 *
 * CPF e telefone devem chegar sem máscara (apenas dígitos):
 *   CPF = 11 chars, telefone = 11 chars (com DDD).
 * ─────────────────────────────────────────────────────────────────────────────
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
