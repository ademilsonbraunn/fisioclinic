-- OBSOLETO — use database/setup.sql para instalações novas.
-- ============================================================
--  Migration: ajustes na tabela evolucoes para M5
--  Aplica sobre schema_modulos_2a7.sql
-- ============================================================

-- 1. Tornar fisioterapeuta_id opcional (campo clínico nem sempre preenchido)
ALTER TABLE evolucoes ALTER COLUMN fisioterapeuta_id DROP NOT NULL;

-- 2. Converter aparelhos de JSONB para TEXT (texto livre do terapeuta)
ALTER TABLE evolucoes ALTER COLUMN aparelhos TYPE TEXT USING aparelhos::TEXT;

-- 3. Adicionar coluna num_sessao (número ordinal da sessão no tratamento)
ALTER TABLE evolucoes ADD COLUMN IF NOT EXISTS num_sessao INTEGER NOT NULL DEFAULT 1;

-- 4. Adicionar coluna observacoes
ALTER TABLE evolucoes ADD COLUMN IF NOT EXISTS observacoes TEXT;

-- 5. Adicionar coluna plano_tratamento_id (vínculo opcional ao plano ativo)
ALTER TABLE evolucoes ADD COLUMN IF NOT EXISTS plano_tratamento_id UUID REFERENCES planos_tratamento(id);

-- 6. Adicionar coluna updated_at (controle de auditoria)
ALTER TABLE evolucoes ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- Índice para o vínculo com plano de tratamento
CREATE INDEX IF NOT EXISTS idx_evolucoes_plano ON evolucoes(plano_tratamento_id);
