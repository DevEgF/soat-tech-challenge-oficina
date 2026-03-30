package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class Veiculo(
	val id: UUID,
	val customerId: UUID,
	val licensePlate: PlacaVeiculo,
	val brand: String,
	val model: String,
	val year: Int,
)
