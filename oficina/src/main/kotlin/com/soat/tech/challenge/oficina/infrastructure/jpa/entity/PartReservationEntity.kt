package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "reservas_peca_os")
class PartReservationEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ordem_servico_id", nullable = false)
	var workOrder: WorkOrderEntity? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "peca_id", nullable = false)
	var part: PartEntity? = null,
	@Column(name = "quantidade", nullable = false)
	var quantity: Int = 0,
	@Column(nullable = false, length = 20)
	var status: String = "",
)
