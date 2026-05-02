ALTER TABLE pecas ADD COLUMN IF NOT EXISTS ponto_reposicao INT;

ALTER TABLE ordens_servico ADD COLUMN IF NOT EXISTS plano_submetido_em TIMESTAMP;
ALTER TABLE ordens_servico ADD COLUMN IF NOT EXISTS aprovacao_interna_em TIMESTAMP;
ALTER TABLE ordens_servico ADD COLUMN IF NOT EXISTS cancelada_em TIMESTAMP;

CREATE TABLE IF NOT EXISTS reservas_peca_os (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	ordem_servico_id VARCHAR(36) NOT NULL,
	peca_id VARCHAR(36) NOT NULL,
	quantidade INT NOT NULL,
	status VARCHAR(20) NOT NULL,
	CONSTRAINT fk_reserva_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
	CONSTRAINT fk_reserva_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);

CREATE INDEX IF NOT EXISTS idx_reservas_os ON reservas_peca_os (ordem_servico_id);
CREATE INDEX IF NOT EXISTS idx_reservas_peca_status ON reservas_peca_os (peca_id, status);
