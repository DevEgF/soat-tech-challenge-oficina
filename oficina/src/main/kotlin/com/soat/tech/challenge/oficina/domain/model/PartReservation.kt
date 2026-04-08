package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class PartReservation(
	val id: UUID,
	val workOrderId: UUID,
	val partId: UUID,
	val quantity: Int,
	val status: PartReservationStatus,
)
