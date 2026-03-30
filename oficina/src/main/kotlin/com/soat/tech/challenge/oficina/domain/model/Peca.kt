package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

/** Part or supply with stock control. */
data class Peca(
	val id: UUID,
	val code: String,
	val name: String,
	val priceCents: Long,
	val stockQuantity: Int,
) {
	init {
		require(stockQuantity >= 0) { "Stock cannot be negative" }
	}

	fun withAdjustedStock(delta: Int): Peca {
		val next = stockQuantity + delta
		require(next >= 0) { "Resulting stock cannot be negative" }
		return copy(stockQuantity = next)
	}
}
