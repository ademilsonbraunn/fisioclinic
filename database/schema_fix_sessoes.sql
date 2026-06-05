-- ============================================================
--  Fix: cria as tabelas que falharam por depender de sessoes
--  A regra de sobreposicao de sala/horario e aplicada
--  no service (PG 11 nao suporta interval em EXCLUDE gist)
-- ============================================================

-- -- Sessoes -------------------------------------------------
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
    numero_sessao       INT,

    status              VARCHAR(15) NOT NULL DEFAULT 'agendado'
                            CHECK (status IN ('agendado','confirmado','realizado','faltou','cancelado')),
    motivo_cancelamento TEXT,
    observacoes         TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ
    -- Regra de nao sobreposicao de sala/horario aplicada no SessaoService
);

-- -- Evolucoes Clinicas (SOAP) --------------------------------
CREATE TABLE IF NOT EXISTS evolucoes (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sessao_id             UUID        NOT NULL REFERENCES sessoes(id),
    paciente_id           UUID        NOT NULL REFERENCES pacientes(id),
    fisioterapeuta_id     UUID        NOT NULL REFERENCES fisioterapeutas(id),
    data_hora             TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    subjetivo             TEXT        NOT NULL,
    objetivo              TEXT        NOT NULL,
    avaliacao_clinica     TEXT        NOT NULL,
    plano                 TEXT        NOT NULL,

    tecnicas_realizadas   JSONB,
    aparelhos             JSONB,
    tempo_atendimento_min INT,

    eva_antes             SMALLINT    CHECK (eva_antes  BETWEEN 0 AND 10),
    eva_depois            SMALLINT    CHECK (eva_depois BETWEEN 0 AND 10),
    codigo_tuss           VARCHAR(20),

    assinado_em           TIMESTAMPTZ,  -- imutavel apos assinatura (CFM 1.821/07)
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_evolucao_sessao UNIQUE (sessao_id)
);

-- -- Fotos da Evolucao ----------------------------------------
CREATE TABLE IF NOT EXISTS fotos_evolucao (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    evolucao_id UUID        NOT NULL REFERENCES evolucoes(id) ON DELETE CASCADE,
    tipo        VARCHAR(15) NOT NULL DEFAULT 'outro'
                    CHECK (tipo IN ('antes','depois','comparativo','outro')),
    url         TEXT        NOT NULL,
    descricao   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- -- Altas ---------------------------------------------------
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
--  INDICES (sessoes e tabelas dependentes)
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_sessoes_paciente      ON sessoes(paciente_id);
CREATE INDEX IF NOT EXISTS idx_sessoes_fisio         ON sessoes(fisioterapeuta_id);
CREATE INDEX IF NOT EXISTS idx_sessoes_data          ON sessoes(data_hora_inicio);
CREATE INDEX IF NOT EXISTS idx_sessoes_status        ON sessoes(status);
CREATE INDEX IF NOT EXISTS idx_sessoes_sala_data     ON sessoes(sala_id, data_hora_inicio);
CREATE INDEX IF NOT EXISTS idx_evolucoes_sessao      ON evolucoes(sessao_id);
CREATE INDEX IF NOT EXISTS idx_evolucoes_paciente    ON evolucoes(paciente_id);
CREATE INDEX IF NOT EXISTS idx_evolucoes_data        ON evolucoes(data_hora);
CREATE INDEX IF NOT EXISTS idx_altas_paciente        ON altas(paciente_id);
