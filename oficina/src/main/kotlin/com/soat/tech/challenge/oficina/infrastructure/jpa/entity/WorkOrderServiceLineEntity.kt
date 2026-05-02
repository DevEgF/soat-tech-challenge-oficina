package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ordem_servico_linhas_servico")
class WorkOrderServiceLineEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ordem_servico_id", nullable = false)
	var workOrder: WorkOrderEntity? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "servico_catalogo_id", nullable = false)
	var catalogService: CatalogServiceEntity? = null,
	@Column(nullable = false, name = "quantidade")
	var quantity: Int = 0,
	@Column(nullable = false, name = "preco_unitario_centavos")
	var unitPriceCents: Long = 0,
)
