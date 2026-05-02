-- Audit timestamp: tracks when an OS was reverted from AGUARDANDO_APROVACAO to EM_DIAGNOSTICO
ALTER TABLE ordens_servico ADD COLUMN IF NOT EXISTS retornado_diagnostico_em TIMESTAMPTZ;

-- Indexes on foreign keys missing from V1 (avoid full table scans on common queries)
CREATE INDEX IF NOT EXISTS idx_veiculos_cliente    ON veiculos (cliente_id);
CREATE INDEX IF NOT EXISTS idx_os_cliente          ON ordens_servico (cliente_id);
CREATE INDEX IF NOT EXISTS idx_os_veiculo          ON ordens_servico (veiculo_id);
