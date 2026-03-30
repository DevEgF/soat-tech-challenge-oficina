package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

/** Part or supply with stock control. */
data class Peca(
	val id: UUID,
	val code: String,
	val name: String,
	val priceCents: Long,
	val stockQuantity: Int,
	/** When non-null and stock is at or below this value, almoxarife sees low-stock alert. */
	val replenishmentPoint: Int? = null,
) {
	init {
		require(stockQuantity >= 0) { "Stock cannot be negative" }
		require(replenishmentPoint == null || replenishmentPoint >= 0) { "Replenishment point cannot be negative" }
	}

	fun withAdjustedStock(delta: Int): Peca {
		val next = stockQuantity + delta
		require(next >= 0) { "Resulting stock cannot be negative" }
		return copy(stockQuantity = next)
	}
}
