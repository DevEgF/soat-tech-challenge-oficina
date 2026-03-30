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
class VeiculoEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	var cliente: ClienteEntity? = null,
	@Column(nullable = false, unique = true, length = 10)
	var placa: String = "",
	@Column(nullable = false, length = 100)
	var marca: String = "",
	@Column(nullable = false, length = 100)
	var modelo: String = "",
	@Column(nullable = false)
	var ano: Int = 0,
)
