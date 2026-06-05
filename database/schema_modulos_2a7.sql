-- ============================================================
--  FisioClinic -- Schema PostgreSQL
--  Modulos 2 a 7: Anamnese, Plano de Tratamento, Agendamento,
--                 Evolucao Clinica, Faturamento e Alta
--
--  Pre-requisito: schema.sql (Modulo 1) ja aplicado.
--  Execute como superusuario (postgres).
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "btree_gist";   -- necessario para EXCLUDE de horarios

-- ============================================================
--  MODULO 2 e 3 -- Profissionais, Salas, Anamnese e Plano
-- ============================================================

-- -- Fisioterapeutas -------------------------------------------
CREATE TABLE IF NOT EXISTS fisioterapeutas (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       TEXT        NOT NULL,
    crf        VARCHAR(15) NOT NULL,
    email      TEXT,
    telefone   VARCHAR(11),
    ativo      BOOLEAN     NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_fisio_crf   UNIQUE (crf),
    CONSTRAINT uq_fisio_email UNIQUE (email)
);

-- -- Salas e Boxes --------------------------------------------
CREATE TABLE IF NOT EXISTS salas (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       TEXT        NOT NULL,
    tipo       VARCHAR(20) NOT NULL DEFAULT 'box'
                   CHECK (tipo IN ('box','sala_individual','sala_grupo')),
    capacidade INT         NOT NULL DEFAULT 1,
    ativo      BOOLEAN     NOT NULL DEFAULT true,
    CONSTRAINT uq_sala_nome UNIQUE (nome)
);

-- -- Anamneses ------------------------------------------------
CREATE TABLE IF NOT EXISTS anamneses (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id            UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id      UUID        REFERENCES fisioterapeutas(id),
    data_avaliacao         DATE        NOT NULL DEFAULT CURRENT_DATE,

    -- Queixa e historico (obrigatorios)
    queixa_principal       TEXT        NOT NULL,
    historico_doenca_atual TEXT        NOT NULL,

    -- Campos opcionais
    tempo_inicio_sintomas  TEXT,
    doencas_preexistentes  TEXT,
    cirurgias_anteriores   TEXT,
    medicamentos           TEXT,
    alergias               TEXT,
    historico_familiar     TEXT,

    -- Avaliacao fisica (JSONB para campos dinamicos)
    -- Estrutura: { postura, adm, forca_muscular, eva (0-10),
    --              testes_especiais[], goniometria }
    avaliacao_fisica       JSONB,

    observacoes            TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ
);

-- -- Arquivos da Anamnese (exames, laudos, encaminhamentos) ---
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

-- -- Planos de Tratamento ------------------------------------
CREATE TABLE IF NOT EXISTS planos_tratamento (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id           UUID        NOT NULL REFERENCES pacientes(id),
    anamnese_id           UUID        REFERENCES anamneses(id),
    fisioterapeuta_id     UUID        REFERENCES fisioterapeutas(id),

    -- Diagnostico (obrigatorios)
    diagnostico_cif       TEXT        NOT NULL,
    cid10                 VARCHAR(10),
    objetivos_curto_prazo TEXT        NOT NULL,
    objetivos_longo_prazo TEXT        NOT NULL,

    -- Tecnicas e recursos
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

-- -- Termos de Consentimento (TCLE) --------------------------
CREATE TABLE IF NOT EXISTS termos_consentimento (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id    UUID        NOT NULL REFERENCES pacientes(id),
    plano_id       UUID        REFERENCES planos_tratamento(id),
    tipo           VARCHAR(20) NOT NULL DEFAULT 'tcle'
                       CHECK (tipo IN ('tcle','autorizacao','outro')),
    conteudo       TEXT,
    assinado_em    TIMESTAMPTZ,
    assinatura_url TEXT,       -- URL da imagem/PDF da assinatura
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
--  MODULO 4 -- Agendamento / Sessoes
-- ============================================================

CREATE TABLE IF NOT EXISTS sessoes (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id         UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id   UUID        NOT NULL REFERENCES fisioterapeutas(id),
    sala_id             UUID        REFERENCES salas(id),
    plano_id            UUID        REFERENCES planos_tratamento(id),
    convenio_id         UUID        REFERENCES convenios_paciente(id),

    tipo_sessao         VARCHAR(20) NOT NULL DEFAULT 'sessao'
                            CHECK (tipo_sessao IN ('avaliacao','retorno','sessao')),
    data_hora_inicio    TIMESTAMPTZ NOT NULL,
    data_hora_fim       TIMESTAMPTZ,
    duracao_minutos     INT,
    numero_sessao       INT,        -- numero sequencial da sessao no plano

    status              VARCHAR(15) NOT NULL DEFAULT 'agendado'
                            CHECK (status IN ('agendado','confirmado','realizado','faltou','cancelado')),
    motivo_cancelamento TEXT,
    observacoes         TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,

    -- Regra CFM: nao pode haver dois agendamentos na mesma sala no mesmo horario
    -- Cancelas e faltas sao excluidos da verificacao
    CONSTRAINT uq_sala_horario EXCLUDE USING gist (
        sala_id WITH =,
        tstzrange(
            data_hora_inicio,
            COALESCE(data_hora_fim, data_hora_inicio + interval '1 hour'),
            '[)'
        ) WITH &&
    ) WHERE (sala_id IS NOT NULL AND status NOT IN ('cancelado','faltou'))
);

-- ============================================================
--  MODULO 5 -- Evolucao Clinica (SOAP)
-- ============================================================

CREATE TABLE IF NOT EXISTS evolucoes (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sessao_id             UUID        NOT NULL REFERENCES sessoes(id),
    paciente_id           UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id     UUID        NOT NULL REFERENCES fisioterapeutas(id),
    data_hora             TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- SOAP (todos obrigatorios)
    subjetivo             TEXT        NOT NULL,  -- queixa do paciente
    objetivo              TEXT        NOT NULL,  -- achados clinicos observaveis
    avaliacao_clinica     TEXT        NOT NULL,  -- interpretacao do fisioterapeuta
    plano                 TEXT        NOT NULL,  -- conduta para proxima sessao

    -- Tecnicas realizadas
    -- JSONB: ["Massagem", "TENS 80Hz 20min"]
    tecnicas_realizadas   JSONB,

    -- Aparelhos e parametros
    -- JSONB: [{"aparelho":"TENS","parametros":"80Hz, 200us, 20min"}]
    aparelhos             JSONB,

    tempo_atendimento_min INT,

    -- EVA -- Escala Visual Analogica de dor (0 a 10)
    eva_antes             SMALLINT    CHECK (eva_antes  BETWEEN 0 AND 10),
    eva_depois            SMALLINT    CHECK (eva_depois BETWEEN 0 AND 10),

    -- Codigo TUSS/CBHPM para faturamento de convenio
    codigo_tuss           VARCHAR(20),

    -- Imutabilidade apos assinatura (Resolucao CFM 1.821/07)
    -- Uma vez assinado, o registro nao pode ser alterado
    assinado_em           TIMESTAMPTZ,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Regra: apenas uma evolucao por sessao
    CONSTRAINT uq_evolucao_sessao UNIQUE (sessao_id)
);

-- -- Fotos comparativas da evolucao --------------------------
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
--  MODULO 6 -- Alta e Relatorios
-- ============================================================

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

    -- Campos opcionais
    orientacoes_domiciliares TEXT,
    relatorio_evolucao       TEXT,
    relatorio_medico         TEXT,        -- para encaminhar ao medico solicitante
    num_sessoes_realizadas   INT,
    agendamento_retorno      DATE,

    -- Pesquisa de satisfacao
    satisfacao_nota          SMALLINT    CHECK (satisfacao_nota BETWEEN 1 AND 5),
    satisfacao_comentario    TEXT,

    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
--  INDICES
-- ============================================================

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
CREATE INDEX IF NOT EXISTS idx_altas_paciente           ON altas(paciente_id);

-- ============================================================
--  DADOS DE EXEMPLO (desenvolvimento)
-- ============================================================

INSERT INTO fisioterapeutas (nome, crf, email, telefone) VALUES
    ('Dra. Ana Paula Ramos',  'SP-123456', 'ana.ramos@fisioclinic.com',   '11988887777'),
    ('Dr. Ricardo Souza',     'SP-654321', 'ricardo.souza@fisioclinic.com','11977776666')
ON CONFLICT DO NOTHING;

INSERT INTO salas (nome, tipo, capacidade) VALUES
    ('Box 1',              'box',             1),
    ('Box 2',              'box',             1),
    ('Box 3',              'box',             1),
    ('Sala de Pilates',    'sala_grupo',      8),
    ('Sala Individual',    'sala_individual', 1)
ON CONFLICT DO NOTHING;
