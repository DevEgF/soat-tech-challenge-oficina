package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.CatalogService
import java.util.Optional
import java.util.UUID

interface CatalogServiceRepository {
	fun save(servico: CatalogService): CatalogService
	fun findById(id: UUID): Optional<CatalogService>
	fun findAll(): List<CatalogService>
	fun deleteById(id: UUID)
}
