-- OBSOLETO — use database/setup.sql para instalações novas.
-- ============================================================
--  Migração: normaliza enums da tabela sessoes para UPPERCASE
--  e adiciona os valores REAVALIACAO e ALTA usados pelo frontend.
--  Rodar UMA ÚNICA VEZ antes de iniciar o backend do Módulo 4.
-- ============================================================

-- 1. Remove constraints antigas (lowercase)
ALTER TABLE sessoes
    DROP CONSTRAINT IF EXISTS sessoes_tipo_sessao_check;

ALTER TABLE sessoes
    DROP CONSTRAINT IF EXISTS sessoes_status_check;

-- 2. Normaliza linhas existentes (se houver dados)
UPDATE sessoes SET
    tipo_sessao = UPPER(tipo_sessao),
    status      = UPPER(status);

-- 3. Atualiza defaults para UPPERCASE
ALTER TABLE sessoes ALTER COLUMN tipo_sessao SET DEFAULT 'SESSAO';
ALTER TABLE sessoes ALTER COLUMN status       SET DEFAULT 'AGENDADO';

-- 4. Recria constraints com UPPERCASE + novos valores
ALTER TABLE sessoes
    ADD CONSTRAINT sessoes_tipo_sessao_check
        CHECK (tipo_sessao IN ('AVALIACAO', 'SESSAO', 'REAVALIACAO', 'ALTA'));

ALTER TABLE sessoes
    ADD CONSTRAINT sessoes_status_check
        CHECK (status IN ('AGENDADO', 'CONFIRMADO', 'REALIZADO', 'FALTOU', 'CANCELADO'));

-- 5. Garante permissão ao usuário fisio
GRANT ALL PRIVILEGES ON TABLE sessoes TO fisio;
