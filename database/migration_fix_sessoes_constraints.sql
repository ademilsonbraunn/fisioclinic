-- ============================================================
-- Migration: Corrige CHECK constraints da tabela sessoes
-- Problema: Java @Enumerated(STRING) persiste valores em MAIUSCULAS,
--           mas os constraints foram definidos com minusculas.
-- ============================================================

BEGIN;

-- ── 1. Dropar CHECK constraints existentes ────────────────────────────────

ALTER TABLE sessoes DROP CONSTRAINT IF EXISTS sessoes_tipo_sessao_check;
ALTER TABLE sessoes DROP CONSTRAINT IF EXISTS sessoes_status_check;

-- ── 2. Recriar CHECK com valores maiusculos e enum correto ─────────────────

ALTER TABLE sessoes
    ADD CONSTRAINT sessoes_tipo_sessao_check
    CHECK (tipo_sessao IN ('AVALIACAO', 'SESSAO', 'REAVALIACAO', 'ALTA'));

ALTER TABLE sessoes
    ADD CONSTRAINT sessoes_status_check
    CHECK (status IN ('AGENDADO', 'CONFIRMADO', 'REALIZADO', 'FALTOU', 'CANCELADO'));

-- ── 3. Corrigir DEFAULT values ─────────────────────────────────────────────

ALTER TABLE sessoes ALTER COLUMN tipo_sessao SET DEFAULT 'SESSAO';
ALTER TABLE sessoes ALTER COLUMN status SET DEFAULT 'AGENDADO';

-- ── 4. EXCLUDE constraint de sala ─────────────────────────────────────────
-- A verificacao de conflito de sala e feita no SessaoService.java (Java).
-- O constraint de banco exige btree_gist extension que pode nao estar habilitada.
-- Mantemos apenas a protecao em nivel de aplicacao.

-- ── 5. Adicionar colunas senha_hash e perfil em fisioterapeutas ────────────
-- O model Java espera essas colunas para autenticacao JWT.

ALTER TABLE fisioterapeutas
    ADD COLUMN IF NOT EXISTS senha_hash TEXT,
    ADD COLUMN IF NOT EXISTS perfil     VARCHAR(20) NOT NULL DEFAULT 'FISIOTERAPEUTA'
        CHECK (perfil IN ('FISIOTERAPEUTA', 'ADMIN'));

-- ── 6. Inserir fisioterapeuta administrador padrao (se nao existir) ─────────
-- Senha: admin123 (BCrypt hash)
INSERT INTO fisioterapeutas (nome, crf, email, telefone, ativo, senha_hash, perfil)
SELECT 'Administrador', '000000-F', 'admin@fisioclinic.com', '00000000000',
       true,
       '$2a$10$k62lCruo7YBo4FSQ0DSdBerg4pXrXs9TqTvpwWh9d.SmzoAxXsl6G',
       'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM fisioterapeutas WHERE email = 'admin@fisioclinic.com'
);

COMMIT;
