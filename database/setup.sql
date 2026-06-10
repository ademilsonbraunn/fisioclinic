-- ============================================================
--  FisioClinic — Setup completo do banco de dados
--  Versão: v1.4
--
--  USO: execute este arquivo em um banco PostgreSQL 11+ vazio.
--
--    psql -U postgres -p 5411 -d fisioclinic -f setup.sql
--
--  Este arquivo substitui todos os arquivos anteriores:
--    schema.sql, schema_modulos_2a7.sql, schema_fix_sessoes.sql,
--    schema_auth.sql, schema_atualizacoes.sql,
--    migrate_sessoes_enums.sql,
--    migration_fix_sessoes_constraints.sql,
--    migration_evolucoes_ajustes.sql
--
--  Pré-requisito: banco 'fisioclinic' criado e usuário 'fisio' existente.
--    CREATE DATABASE fisioclinic;
--    CREATE USER fisio WITH PASSWORD 'fisio123';
--    GRANT ALL PRIVILEGES ON DATABASE fisioclinic TO fisio;
-- ============================================================

-- ── Extensões ────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- gen_random_uuid()

-- ============================================================
--  MÓDULO 1 — Cadastro de Pacientes
-- ============================================================

CREATE TABLE IF NOT EXISTS pacientes (
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

-- [M1] Contato de emergência vinculado ao paciente
CREATE TABLE IF NOT EXISTS contatos_emergencia (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id UUID        NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    nome        TEXT,
    parentesco  VARCHAR(20)
                    CHECK (parentesco IN ('CONJUGE','PAI_MAE','FILHO_FILHA','IRMAO_IRMA','AMIGO','OUTRO')),
    telefone    CHAR(11),

    CONSTRAINT uq_contato_por_paciente UNIQUE (paciente_id)
);

-- [M1] Convênio / forma de pagamento do paciente
CREATE TABLE IF NOT EXISTS convenios_paciente (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id            UUID        NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    tipo_pagamento         VARCHAR(15) NOT NULL
                               CHECK (tipo_pagamento IN ('PARTICULAR','CONVENIO')),
    nome_convenio          TEXT,
    num_carteirinha        TEXT,
    validade_plano         CHAR(7),    -- formato "YYYY-MM"
    responsavel_financeiro TEXT
);

-- ============================================================
--  MÓDULOS 2 e 3 — Profissionais, Salas, Anamnese e Plano
-- ============================================================

-- [M2/M3/M4/M5] Fisioterapeutas — inclui campos de autenticação JWT
CREATE TABLE IF NOT EXISTS fisioterapeutas (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        TEXT        NOT NULL,
    crf         VARCHAR(15) NOT NULL,
    email       TEXT,
    telefone    VARCHAR(11),
    ativo       BOOLEAN     NOT NULL DEFAULT true,
    senha_hash  TEXT,
    perfil      VARCHAR(20) NOT NULL DEFAULT 'FISIOTERAPEUTA'
                    CHECK (perfil IN ('FISIOTERAPEUTA', 'ADMIN')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    CONSTRAINT uq_fisio_crf   UNIQUE (crf),
    CONSTRAINT uq_fisio_email UNIQUE (email)
);

-- [M4] Salas e boxes de atendimento
CREATE TABLE IF NOT EXISTS salas (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       TEXT        NOT NULL,
    tipo       VARCHAR(20) NOT NULL DEFAULT 'BOX'
                   CHECK (tipo IN ('BOX','SALA_INDIVIDUAL','SALA_GRUPO')),
    capacidade INT         NOT NULL DEFAULT 1,
    ativo      BOOLEAN     NOT NULL DEFAULT true,
    CONSTRAINT uq_sala_nome UNIQUE (nome)
);

-- [M2] Anamnese e avaliação inicial — queixa, histórico e avaliação física
CREATE TABLE IF NOT EXISTS anamneses (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id            UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id      UUID        REFERENCES fisioterapeutas(id),
    data_avaliacao         DATE        NOT NULL DEFAULT CURRENT_DATE,

    queixa_principal       TEXT        NOT NULL,
    historico_doenca_atual TEXT        NOT NULL,

    tempo_inicio_sintomas  TEXT,
    doencas_preexistentes  TEXT,
    cirurgias_anteriores   TEXT,
    medicamentos           TEXT,
    alergias               TEXT,
    historico_familiar     TEXT,

    -- JSONB: { postura, adm, forca_muscular, eva (0-10), testes_especiais[], goniometria }
    avaliacao_fisica       JSONB,

    observacoes            TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ
);

-- [M2] Arquivos vinculados à anamnese (exames, laudos, encaminhamentos)
CREATE TABLE IF NOT EXISTS arquivos_anamnese (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    anamnese_id   UUID        NOT NULL REFERENCES anamneses(id) ON DELETE CASCADE,
    tipo          VARCHAR(20) NOT NULL
                      CHECK (tipo IN ('exame','laudo','encaminhamento','outro')),
    nome_arquivo  TEXT        NOT NULL,
    url           TEXT        NOT NULL,
    tamanho_bytes BIGINT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- [M3] Plano de tratamento — fornece plano_id para M4 (Agendamento)
CREATE TABLE IF NOT EXISTS planos_tratamento (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id           UUID        NOT NULL REFERENCES pacientes(id),
    anamnese_id           UUID        REFERENCES anamneses(id),
    fisioterapeuta_id     UUID        REFERENCES fisioterapeutas(id),

    diagnostico_cif       TEXT        NOT NULL,
    cid10                 VARCHAR(10),
    objetivos_curto_prazo TEXT        NOT NULL,
    objetivos_longo_prazo TEXT        NOT NULL,

    -- JSONB: ["Cinesioterapia", "TENS", "Ultrassom"]
    tecnicas              JSONB,

    frequencia_semanal    INT         NOT NULL,
    num_sessoes_estimado  INT         NOT NULL,
    hipoteses_tratamento  TEXT,
    data_inicio           DATE        NOT NULL DEFAULT CURRENT_DATE,
    data_previsao_alta    DATE,

    status                VARCHAR(15) NOT NULL DEFAULT 'ativo'
                              CHECK (status IN ('ativo','concluido','cancelado')),

    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ
);

-- [M3] TCLE e termos de consentimento assinados pelo paciente
CREATE TABLE IF NOT EXISTS termos_consentimento (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id    UUID        NOT NULL REFERENCES pacientes(id),
    plano_id       UUID        REFERENCES planos_tratamento(id),
    tipo           VARCHAR(20) NOT NULL DEFAULT 'tcle'
                       CHECK (tipo IN ('tcle','autorizacao','outro')),
    conteudo       TEXT,
    assinado_em    TIMESTAMPTZ,
    assinatura_url TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
--  MÓDULO 4 — Agendamento / Sessões
-- ============================================================

-- [M4] Sessões agendadas — consome paciente_id (M1) e plano_id (M3)
-- Nota: a regra de não sobreposição de sala/horário é validada em SessaoService.java
--       (PG 11 não suporta EXCLUDE com interval em btree_gist)
CREATE TABLE IF NOT EXISTS sessoes (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id         UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id   UUID        NOT NULL REFERENCES fisioterapeutas(id),
    sala_id             UUID        REFERENCES salas(id),
    plano_id            UUID        REFERENCES planos_tratamento(id),
    convenio_id         UUID        REFERENCES convenios_paciente(id),

    tipo_sessao         VARCHAR(20) NOT NULL DEFAULT 'SESSAO'
                            CHECK (tipo_sessao IN ('AVALIACAO','SESSAO','REAVALIACAO','ALTA')),
    data_hora_inicio    TIMESTAMPTZ NOT NULL,
    data_hora_fim       TIMESTAMPTZ,
    duracao_minutos     INT,
    numero_sessao       INT,        -- número sequencial da sessão no plano

    status              VARCHAR(15) NOT NULL DEFAULT 'AGENDADO'
                            CHECK (status IN ('AGENDADO','CONFIRMADO','REALIZADO','FALTOU','CANCELADO')),
    motivo_cancelamento TEXT,
    observacoes         TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ
);

-- ============================================================
--  MÓDULO 5 — Evolução Clínica (SOAP)
-- ============================================================

-- [M5] Evolução SOAP vinculada a uma sessão (M4) — dados enviados para M6 (Alta)
-- Regra CFM 1.821/07: imutável após assinatura (campo assinado_em)
CREATE TABLE IF NOT EXISTS evolucoes (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sessao_id             UUID        NOT NULL REFERENCES sessoes(id),
    paciente_id           UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id     UUID        REFERENCES fisioterapeutas(id),  -- opcional
    data_hora             TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- SOAP (todos obrigatórios)
    subjetivo             TEXT        NOT NULL,  -- queixa do paciente
    objetivo              TEXT        NOT NULL,  -- achados clínicos observáveis
    avaliacao_clinica     TEXT        NOT NULL,  -- interpretação do fisioterapeuta
    plano                 TEXT        NOT NULL,  -- conduta para próxima sessão

    -- JSONB: ["Massagem", "TENS 80Hz 20min"]
    tecnicas_realizadas   JSONB,

    -- Texto livre com aparelhos e parâmetros usados na sessão
    aparelhos             TEXT,

    tempo_atendimento_min INT,
    num_sessao            INT         NOT NULL DEFAULT 1,  -- número ordinal no plano

    -- EVA — Escala Visual Analógica de dor (0 a 10)
    eva_antes             SMALLINT    CHECK (eva_antes  BETWEEN 0 AND 10),
    eva_depois            SMALLINT    CHECK (eva_depois BETWEEN 0 AND 10),

    codigo_tuss           VARCHAR(20),
    observacoes           TEXT,

    -- Vínculo opcional ao plano de tratamento ativo (M3)
    plano_tratamento_id   UUID        REFERENCES planos_tratamento(id),

    -- Imutabilidade após assinatura (Resolução CFM 1.821/07)
    assinado_em           TIMESTAMPTZ,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ,

    -- Regra: apenas uma evolução por sessão
    CONSTRAINT uq_evolucao_sessao UNIQUE (sessao_id)
);

-- [M5] Fotos comparativas vinculadas à evolução
CREATE TABLE IF NOT EXISTS fotos_evolucao (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    evolucao_id UUID        NOT NULL REFERENCES evolucoes(id) ON DELETE CASCADE,
    tipo        VARCHAR(15) NOT NULL DEFAULT 'outro'
                    CHECK (tipo IN ('antes','depois','comparativo','outro')),
    url         TEXT        NOT NULL,
    descricao   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
--  MÓDULO 6 — Alta e Relatórios
-- ============================================================

-- [M6] Alta do paciente — consome histórico de evoluções (M5)
CREATE TABLE IF NOT EXISTS altas (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id              UUID        NOT NULL REFERENCES pacientes(id),
    plano_id                 UUID        REFERENCES planos_tratamento(id),
    fisioterapeuta_id        UUID        REFERENCES fisioterapeutas(id),

    data_alta                DATE        NOT NULL DEFAULT CURRENT_DATE,
    motivo                   VARCHAR(25) NOT NULL
                                 CHECK (motivo IN (
                                     'alta_clinica','alta_administrativa',
                                     'desistencia','encaminhamento','obito'
                                 )),
    resultado_objetivos      TEXT        NOT NULL,

    orientacoes_domiciliares TEXT,
    relatorio_evolucao       TEXT,
    relatorio_medico         TEXT,
    num_sessoes_realizadas   INT,
    agendamento_retorno      DATE,

    satisfacao_nota          SMALLINT    CHECK (satisfacao_nota BETWEEN 1 AND 5),
    satisfacao_comentario    TEXT,

    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
--  CARD DE ATUALIZAÇÕES — novidades visíveis ao usuário final
-- ============================================================

CREATE TABLE IF NOT EXISTS atualizacoes_sistema (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo          VARCHAR(100) NOT NULL,
    descricao       TEXT         NOT NULL,
    versao          VARCHAR(10)  NOT NULL,
    tipo            VARCHAR(20)  NOT NULL
                        CHECK (tipo IN ('NOVO_RECURSO', 'MELHORIA', 'CORRECAO')),
    data_lancamento DATE         NOT NULL DEFAULT CURRENT_DATE,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
--  ÍNDICES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_pacientes_nome          ON pacientes(nome_completo);
CREATE INDEX IF NOT EXISTS idx_pacientes_cpf           ON pacientes(cpf);
CREATE INDEX IF NOT EXISTS idx_convenios_paciente_id   ON convenios_paciente(paciente_id);
CREATE INDEX IF NOT EXISTS idx_anamneses_paciente       ON anamneses(paciente_id);
CREATE INDEX IF NOT EXISTS idx_anamneses_fisio          ON anamneses(fisioterapeuta_id);
CREATE INDEX IF NOT EXISTS idx_planos_paciente          ON planos_tratamento(paciente_id);
CREATE INDEX IF NOT EXISTS idx_planos_status            ON planos_tratamento(status);
CREATE INDEX IF NOT EXISTS idx_sessoes_paciente         ON sessoes(paciente_id);
CREATE INDEX IF NOT EXISTS idx_sessoes_fisio            ON sessoes(fisioterapeuta_id);
CREATE INDEX IF NOT EXISTS idx_sessoes_data             ON sessoes(data_hora_inicio);
CREATE INDEX IF NOT EXISTS idx_sessoes_status           ON sessoes(status);
CREATE INDEX IF NOT EXISTS idx_sessoes_sala_data        ON sessoes(sala_id, data_hora_inicio);
CREATE INDEX IF NOT EXISTS idx_evolucoes_sessao         ON evolucoes(sessao_id);
CREATE INDEX IF NOT EXISTS idx_evolucoes_paciente       ON evolucoes(paciente_id);
CREATE INDEX IF NOT EXISTS idx_evolucoes_data           ON evolucoes(data_hora);
CREATE INDEX IF NOT EXISTS idx_evolucoes_plano          ON evolucoes(plano_tratamento_id);
CREATE INDEX IF NOT EXISTS idx_altas_paciente           ON altas(paciente_id);
CREATE INDEX IF NOT EXISTS idx_atualizacoes_data        ON atualizacoes_sistema(data_lancamento DESC);

-- ============================================================
--  PERMISSÕES — usuário 'fisio' usado pela aplicação
-- ============================================================

GRANT ALL PRIVILEGES ON TABLE pacientes             TO fisio;
GRANT ALL PRIVILEGES ON TABLE contatos_emergencia   TO fisio;
GRANT ALL PRIVILEGES ON TABLE convenios_paciente    TO fisio;
GRANT ALL PRIVILEGES ON TABLE fisioterapeutas       TO fisio;
GRANT ALL PRIVILEGES ON TABLE salas                 TO fisio;
GRANT ALL PRIVILEGES ON TABLE anamneses             TO fisio;
GRANT ALL PRIVILEGES ON TABLE arquivos_anamnese     TO fisio;
GRANT ALL PRIVILEGES ON TABLE planos_tratamento     TO fisio;
GRANT ALL PRIVILEGES ON TABLE termos_consentimento  TO fisio;
GRANT ALL PRIVILEGES ON TABLE sessoes               TO fisio;
GRANT ALL PRIVILEGES ON TABLE evolucoes             TO fisio;
GRANT ALL PRIVILEGES ON TABLE fotos_evolucao        TO fisio;
GRANT ALL PRIVILEGES ON TABLE altas                 TO fisio;
GRANT SELECT, INSERT, UPDATE ON atualizacoes_sistema TO fisio;

-- ============================================================
--  DADOS DE EXEMPLO (desenvolvimento)
-- ============================================================

-- Pacientes
INSERT INTO pacientes
    (nome_completo, cpf, data_nascimento, sexo, estado_civil, profissao, telefone,
     email, cep, logradouro, numero, complemento, bairro, cidade, uf)
VALUES
    (
        'Maria Aparecida Santos', '12345678901', '1985-03-15',
        'FEMININO', 'CASADO', 'Professora', '11987654321',
        'maria.santos@email.com',
        '01310100', 'Av. Paulista', '1000', 'Apto 42', 'Bela Vista', 'São Paulo', 'SP'
    ),
    (
        'Carlos Eduardo Oliveira', '98765432100', '1972-11-28',
        'MASCULINO', 'SOLTEIRO', 'Engenheiro', '11912345678',
        'carlos.oliveira@empresa.com.br',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL
    )
ON CONFLICT DO NOTHING;

-- Fisioterapeutas
INSERT INTO fisioterapeutas (nome, crf, email, telefone, ativo) VALUES
    ('Dra. Ana Paula Ramos', 'SP-123456', 'ana.ramos@fisioclinic.com',    '11988887777', true),
    ('Dr. Ricardo Souza',    'SP-654321', 'ricardo.souza@fisioclinic.com', '11977776666', true)
ON CONFLICT DO NOTHING;

-- Administrador padrão — login: admin@fisioclinic.com / senha: admin123
INSERT INTO fisioterapeutas (nome, crf, email, telefone, ativo, senha_hash, perfil)
SELECT
    'Administrador', '000000-F', 'admin@fisioclinic.com', '00000000000',
    true,
    '$2a$10$k62lCruo7YBo4FSQ0DSdBerg4pXrXs9TqTvpwWh9d.SmzoAxXsl6G',
    'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM fisioterapeutas WHERE email = 'admin@fisioclinic.com');

-- Salas
INSERT INTO salas (nome, tipo, capacidade) VALUES
    ('Box 1',           'BOX',             1),
    ('Box 2',           'BOX',             1),
    ('Box 3',           'BOX',             1),
    ('Sala de Pilates', 'SALA_GRUPO',      8),
    ('Sala Individual', 'SALA_INDIVIDUAL', 1)
ON CONFLICT DO NOTHING;

-- Histórico de atualizações visíveis ao usuário
INSERT INTO atualizacoes_sistema (titulo, descricao, versao, tipo, data_lancamento) VALUES
(
    U&'Cadastro de Pacientes dispon\00EDvel',
    U&'Cadastre novos pacientes com dados pessoais, conv\00EAnio e contato de emerg\00EAncia. Busque e edite fichas de pacientes j\00E1 cadastrados.',
    'v1.1', 'NOVO_RECURSO', '2026-06-04'
),
(
    U&'Agendamento de Sess\00F5es dispon\00EDvel',
    U&'Agende sess\00F5es para os pacientes, visualize a agenda semanal por fisioterapeuta e sala, e acompanhe o status de cada atendimento.',
    'v1.2', 'NOVO_RECURSO', '2026-06-05'
),
(
    U&'Evolu\00E7\00E3o Cl\00EDnica (SOAP) dispon\00EDvel',
    U&'Registre evolu\00E7\00F5es cl\00EDnicas no formato SOAP para cada sess\00E3o realizada. Inclui escala de dor EVA, t\00E9cnicas utilizadas e par\00E2metros de aparelhos.',
    'v1.3', 'NOVO_RECURSO', '2026-06-09'
),
(
    U&'Alta e Relat\00F3rios dispon\00EDveis',
    U&'Registre a alta do paciente com motivo e resultado do tratamento. Gere relat\00F3rio de evolu\00E7\00E3o e documente orienta\00E7\00F5es domiciliares.',
    'v1.4', 'NOVO_RECURSO', '2026-06-10'
)
ON CONFLICT DO NOTHING;
