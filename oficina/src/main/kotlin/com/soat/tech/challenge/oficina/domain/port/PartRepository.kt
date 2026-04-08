package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.Part
import java.util.Optional
import java.util.UUID

interface PartRepository {
	fun save(peca: Part): Part
	fun findById(id: UUID): Optional<Part>
	fun findByCode(code: String): Optional<Part>
	fun findAll(): List<Part>

	/** Parts with replenishment point set and physical stock at or below that point. */
	fun findAllAtOrBelowReplenishment(): List<Part>

	fun deleteById(id: UUID)
}
