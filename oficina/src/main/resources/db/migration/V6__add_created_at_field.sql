-- Adiciona timestamp de criação da OS para ordenação FIFO na listagem (Fase 2).
ALTER TABLE ordens_servico
    ADD COLUMN criado_em TIMESTAMP NOT NULL DEFAULT NOW();
