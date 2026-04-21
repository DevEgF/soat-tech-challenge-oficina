package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

/** Service offered by the shop (catalog) with price and estimated execution time. */
data class CatalogService(
	val id: UUID,
	val name: String,
	val description: String? = null,
	val priceCents: Long,
	val estimatedMinutes: Int,
)
