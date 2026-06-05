-- ============================================================
--  FisioClinic — Migração de Autenticação
--  Pré-requisito: schema.sql + schema_modulos_2a7.sql aplicados.
--  Execute como superusuário (postgres).
-- ============================================================

-- ── 1. Adicionar campos de autenticação em fisioterapeutas ───

ALTER TABLE fisioterapeutas
    ADD COLUMN IF NOT EXISTS senha_hash TEXT,
    ADD COLUMN IF NOT EXISTS perfil     VARCHAR(20) NOT NULL DEFAULT 'FISIOTERAPEUTA';

ALTER TABLE fisioterapeutas
    DROP CONSTRAINT IF EXISTS fisioterapeutas_perfil_check;

ALTER TABLE fisioterapeutas
    ADD CONSTRAINT fisioterapeutas_perfil_check
        CHECK (perfil IN ('FISIOTERAPEUTA', 'ADMIN'));

-- ── 2. Normalizar coluna 'tipo' de salas para MAIÚSCULAS ────

UPDATE salas SET tipo = UPPER(tipo) WHERE tipo <> UPPER(tipo);

ALTER TABLE salas
    DROP CONSTRAINT IF EXISTS salas_tipo_check;

ALTER TABLE salas
    ADD CONSTRAINT salas_tipo_check
        CHECK (tipo IN ('BOX', 'SALA_INDIVIDUAL', 'SALA_GRUPO'));

ALTER TABLE salas
    ALTER COLUMN tipo SET DEFAULT 'BOX';

-- ── 3. Conceder permissões ao usuário fisio ──────────────────

GRANT ALL PRIVILEGES ON TABLE fisioterapeutas TO fisio;
GRANT ALL PRIVILEGES ON TABLE salas TO fisio;
