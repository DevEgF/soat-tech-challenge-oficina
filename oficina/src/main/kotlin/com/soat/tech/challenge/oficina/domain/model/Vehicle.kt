package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class Vehicle(
	val id: UUID,
	val customerId: UUID,
	val licensePlate: LicensePlate,
	val brand: String,
	val model: String,
	val year: Int,
)
