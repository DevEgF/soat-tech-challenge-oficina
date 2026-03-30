package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class Veiculo(
	val id: UUID,
	val clienteId: UUID,
	val placa: PlacaVeiculo,
	val marca: String,
	val modelo: String,
	val ano: Int,
)
