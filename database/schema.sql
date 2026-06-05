-- ============================================================
--  FisioClinic — Schema PostgreSQL
--  Módulo 1: Cadastro de Paciente
-- ============================================================

-- Extensão para geração de UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Pacientes ────────────────────────────────────────────────
CREATE TABLE pacientes (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo    TEXT        NOT NULL,
    cpf              CHAR(11)    NOT NULL,
    data_nascimento  DATE        NOT NULL,
    sexo             VARCHAR(20) NOT NULL
                        CHECK (sexo IN ('FEMININO','MASCULINO','OUTRO','NAO_INFORMADO')),
    estado_civil     VARCHAR(20)
                        CHECK (estado_civil IN ('SOLTEIRO','CASADO','DIVORCIADO','VIUVO','UNIAO_ESTAVEL')),
    profissao        TEXT,
    foto_url         TEXT,
    email            TEXT,
    telefone         CHAR(11)    NOT NULL,

    -- Endereço
    cep              CHAR(8),
    logradouro       TEXT,
    numero           TEXT,
    complemento      TEXT,
    bairro           TEXT,
    cidade           TEXT,
    uf               CHAR(2),

    -- Auditoria
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ,

    CONSTRAINT uq_pacientes_cpf   UNIQUE (cpf),
    CONSTRAINT uq_pacientes_email UNIQUE (email)
);

-- ── Contatos de emergência ───────────────────────────────────
CREATE TABLE contatos_emergencia (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id UUID        NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    nome        TEXT,
    parentesco  VARCHAR(20)
                    CHECK (parentesco IN ('CONJUGE','PAI_MAE','FILHO_FILHA','IRMAO_IRMA','AMIGO','OUTRO')),
    telefone    CHAR(11),

    CONSTRAINT uq_contato_por_paciente UNIQUE (paciente_id)
);

-- ── Convênios / Pagamento ────────────────────────────────────
CREATE TABLE convenios_paciente (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id            UUID        NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    tipo_pagamento         VARCHAR(15) NOT NULL
                               CHECK (tipo_pagamento IN ('PARTICULAR','CONVENIO')),
    nome_convenio          TEXT,
    num_carteirinha        TEXT,
    validade_plano         CHAR(7),    -- "YYYY-MM"
    responsavel_financeiro TEXT
);

-- ── Índices ──────────────────────────────────────────────────
CREATE INDEX idx_pacientes_nome        ON pacientes(nome_completo);
CREATE INDEX idx_pacientes_cpf         ON pacientes(cpf);
CREATE INDEX idx_convenios_paciente_id ON convenios_paciente(paciente_id);

-- ============================================================
--  Dados de exemplo (desenvolvimento)
-- ============================================================

INSERT INTO pacientes
    (id, nome_completo, cpf, data_nascimento, sexo, estado_civil, profissao, telefone,
     email, cep, logradouro, numero, complemento, bairro, cidade, uf,
     created_at)
VALUES
    (
        gen_random_uuid(),
        'Maria Aparecida Santos',
        '12345678901',
        '1985-03-15',
        'FEMININO', 'CASADO', 'Professora',
        '11987654321',
        'maria.santos@email.com',
        '01310100', 'Av. Paulista', '1000', 'Apto 42', 'Bela Vista', 'São Paulo', 'SP',
        NOW()
    ),
    (
        gen_random_uuid(),
        'Carlos Eduardo Oliveira',
        '98765432100',
        '1972-11-28',
        'MASCULINO', 'SOLTEIRO', 'Engenheiro',
        '11912345678',
        'carlos.oliveira@empresa.com.br',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NOW()
    );
