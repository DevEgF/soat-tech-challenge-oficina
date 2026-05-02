ALTER TABLE ordens_servico
	ADD COLUMN IF NOT EXISTS observacoes_diagnostico VARCHAR(2000);
