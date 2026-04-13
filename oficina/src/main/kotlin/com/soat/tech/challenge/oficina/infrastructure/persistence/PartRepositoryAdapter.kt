package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Part
import com.soat.tech.challenge.oficina.domain.port.PartRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PartJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PartEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class PartRepositoryAdapter(
	private val jpa: PartJpaRepository,
) : PartRepository {

	override fun save(peca: Part): Part {
		val e = PartEntity(
			id = peca.id.toString(),
			code = peca.code,
			name = peca.name,
			priceCents = peca.priceCents,
			stockQuantity = peca.stockQuantity,
			replenishmentPoint = peca.replenishmentPoint,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Part> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByCode(code: String): Optional<Part> =
		Optional.ofNullable(jpa.findByCode(code)).map { it.toDomain() }

	override fun findAll(): List<Part> = jpa.findAll().map { it.toDomain() }

	override fun findAllAtOrBelowReplenishment(): List<Part> =
		jpa.findAtOrBelowReplenishment().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
