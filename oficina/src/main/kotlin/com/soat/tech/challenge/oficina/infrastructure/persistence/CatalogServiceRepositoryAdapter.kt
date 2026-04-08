package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.CatalogService
import com.soat.tech.challenge.oficina.domain.port.CatalogServiceRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.CatalogServiceJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.CatalogServiceEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class CatalogServiceRepositoryAdapter(
	private val jpa: CatalogServiceJpaRepository,
) : CatalogServiceRepository {

	override fun save(servico: CatalogService): CatalogService {
		val e = CatalogServiceEntity(
			id = servico.id.toString(),
			name = servico.name,
			description = servico.description,
			priceCents = servico.priceCents,
			estimatedMinutes = servico.estimatedMinutes,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<CatalogService> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findAll(): List<CatalogService> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
