CREATE TABLE clientes (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	documento VARCHAR(14) NOT NULL,
	nome VARCHAR(255) NOT NULL,
	email VARCHAR(255),
	telefone VARCHAR(50)
);

CREATE UNIQUE INDEX uk_clientes_documento ON clientes (documento);

CREATE TABLE veiculos (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	cliente_id VARCHAR(36) NOT NULL,
	placa VARCHAR(10) NOT NULL,
	marca VARCHAR(100) NOT NULL,
	modelo VARCHAR(100) NOT NULL,
	ano INT NOT NULL,
	CONSTRAINT fk_veiculos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE UNIQUE INDEX uk_veiculos_placa ON veiculos (placa);

CREATE TABLE servicos_catalogo (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	nome VARCHAR(255) NOT NULL,
	descricao VARCHAR(2000),
	preco_centavos BIGINT NOT NULL,
	tempo_estimado_minutos INT NOT NULL
);

CREATE TABLE pecas (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	codigo VARCHAR(100) NOT NULL,
	nome VARCHAR(255) NOT NULL,
	preco_centavos BIGINT NOT NULL,
	quantidade_estoque INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_pecas_codigo ON pecas (codigo);

CREATE TABLE ordens_servico (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	codigo_acompanhamento VARCHAR(64) NOT NULL,
	cliente_id VARCHAR(36) NOT NULL,
	veiculo_id VARCHAR(36) NOT NULL,
	status VARCHAR(40) NOT NULL,
	valor_servicos_centavos BIGINT NOT NULL DEFAULT 0,
	valor_pecas_centavos BIGINT NOT NULL DEFAULT 0,
	valor_total_centavos BIGINT NOT NULL DEFAULT 0,
	diagnosticado_em TIMESTAMP,
	orcamento_enviado_em TIMESTAMP,
	aprovado_em TIMESTAMP,
	execucao_iniciada_em TIMESTAMP,
	finalizada_em TIMESTAMP,
	entregue_em TIMESTAMP,
	CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
	CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
);

CREATE UNIQUE INDEX uk_os_codigo_acompanhamento ON ordens_servico (codigo_acompanhamento);

CREATE TABLE ordem_servico_linhas_servico (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	ordem_servico_id VARCHAR(36) NOT NULL,
	servico_catalogo_id VARCHAR(36) NOT NULL,
	quantidade INT NOT NULL,
	preco_unitario_centavos BIGINT NOT NULL,
	CONSTRAINT fk_oss_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
	CONSTRAINT fk_oss_servico FOREIGN KEY (servico_catalogo_id) REFERENCES servicos_catalogo (id)
);

CREATE TABLE ordem_servico_linhas_peca (
	id VARCHAR(36) NOT NULL PRIMARY KEY,
	ordem_servico_id VARCHAR(36) NOT NULL,
	peca_id VARCHAR(36) NOT NULL,
	quantidade INT NOT NULL,
	preco_unitario_centavos BIGINT NOT NULL,
	CONSTRAINT fk_osp_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
	CONSTRAINT fk_osp_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);
