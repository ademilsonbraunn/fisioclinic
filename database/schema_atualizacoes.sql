-- Módulo: Card de Atualizações do Sistema
-- Tabela que armazena novidades visíveis ao usuário final

CREATE TABLE IF NOT EXISTS atualizacoes_sistema (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo          VARCHAR(100) NOT NULL,
    descricao       TEXT NOT NULL,
    versao          VARCHAR(10) NOT NULL,
    tipo            VARCHAR(20) NOT NULL
                        CHECK (tipo IN ('NOVO_RECURSO', 'MELHORIA', 'CORRECAO')),
    data_lancamento DATE NOT NULL DEFAULT CURRENT_DATE,
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_atualizacoes_data
    ON atualizacoes_sistema (data_lancamento DESC);

GRANT SELECT, INSERT, UPDATE ON atualizacoes_sistema TO fisio;

-- Seed: registros dos módulos já concluídos
-- Unicode escapes garantem encoding correto independente do cliente psql
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
);
