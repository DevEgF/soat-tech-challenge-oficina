package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "veiculos")
class VehicleEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	var customer: CustomerEntity? = null,
	@Column(nullable = false, unique = true, length = 10, name = "placa")
	var licensePlate: String = "",
	@Column(nullable = false, length = 100, name = "marca")
	var brand: String = "",
	@Column(nullable = false, length = 100, name = "modelo")
	var model: String = "",
	@Column(nullable = false, name = "ano")
	var year: Int = 0,
)
