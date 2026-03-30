package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "ordens_servico")
class OrdemServicoEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@Column(nullable = false, unique = true, name = "codigo_acompanhamento", length = 64)
	var codigoAcompanhamento: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	var cliente: ClienteEntity? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "veiculo_id", nullable = false)
	var veiculo: VeiculoEntity? = null,
	@Column(nullable = false, length = 40)
	var status: String = "",
	@Column(nullable = false, name = "valor_servicos_centavos")
	var valorServicosCentavos: Long = 0,
	@Column(nullable = false, name = "valor_pecas_centavos")
	var valorPecasCentavos: Long = 0,
	@Column(nullable = false, name = "valor_total_centavos")
	var valorTotalCentavos: Long = 0,
	@Column(name = "diagnosticado_em")
	var diagnosticadoEm: Instant? = null,
	@Column(name = "orcamento_enviado_em")
	var orcamentoEnviadoEm: Instant? = null,
	@Column(name = "aprovado_em")
	var aprovadoEm: Instant? = null,
	@Column(name = "execucao_iniciada_em")
	var execucaoIniciadaEm: Instant? = null,
	@Column(name = "finalizada_em")
	var finalizadaEm: Instant? = null,
	@Column(name = "entregue_em")
	var entregueEm: Instant? = null,
	@OneToMany(mappedBy = "ordemServico", cascade = [CascadeType.ALL], orphanRemoval = true)
	var linhasServico: MutableSet<OrdemServicoLinhaServicoEntity> = mutableSetOf(),
	@OneToMany(mappedBy = "ordemServico", cascade = [CascadeType.ALL], orphanRemoval = true)
	var linhasPeca: MutableSet<OrdemServicoLinhaPecaEntity> = mutableSetOf(),
)
