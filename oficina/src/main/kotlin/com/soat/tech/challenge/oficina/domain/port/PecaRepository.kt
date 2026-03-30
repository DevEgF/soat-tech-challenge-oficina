package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.Peca
import java.util.Optional
import java.util.UUID

interface PecaRepository {
	fun save(peca: Peca): Peca
	fun findById(id: UUID): Optional<Peca>
	fun findByCode(code: String): Optional<Peca>
	fun findAll(): List<Peca>

	/** Parts with replenishment point set and physical stock at or below that point. */
	fun findAllAtOrBelowReplenishment(): List<Peca>

	fun deleteById(id: UUID)
}
