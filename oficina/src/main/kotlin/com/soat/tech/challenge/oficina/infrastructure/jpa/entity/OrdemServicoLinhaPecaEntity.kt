package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ordem_servico_linhas_peca")
class OrdemServicoLinhaPecaEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ordem_servico_id", nullable = false)
	var ordemServico: OrdemServicoEntity? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "peca_id", nullable = false)
	var peca: PecaEntity? = null,
	@Column(nullable = false)
	var quantidade: Int = 0,
	@Column(nullable = false, name = "preco_unitario_centavos")
	var precoUnitarioCentavos: Long = 0,
)
