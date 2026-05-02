package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.CatalogServiceRequest
import com.soat.tech.challenge.oficina.application.api.dto.CatalogServiceResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.model.CatalogService
import com.soat.tech.challenge.oficina.domain.port.CatalogServiceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CatalogServiceApplicationService(
	private val catalog: CatalogServiceRepository,
) {

	@Transactional
	fun create(req: CatalogServiceRequest): CatalogServiceResponse {
		val s = CatalogService(
			id = UUID.randomUUID(),
			name = req.name.trim(),
			description = req.description?.trim()?.takeIf { it.isNotEmpty() },
			priceCents = req.priceCents!!,
			estimatedMinutes = req.estimatedMinutes!!,
		)
		return catalog.save(s).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<CatalogServiceResponse> = catalog.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): CatalogServiceResponse =
		catalog.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Catalog service not found") }

	@Transactional
	fun update(id: UUID, req: CatalogServiceRequest): CatalogServiceResponse {
		val existing = catalog.findById(id).orElseThrow { NotFoundException("Catalog service not found") }
		val updated = existing.copy(
			name = req.name.trim(),
			description = req.description?.trim()?.takeIf { it.isNotEmpty() },
			priceCents = req.priceCents!!,
			estimatedMinutes = req.estimatedMinutes!!,
		)
		return catalog.save(updated).toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		if (catalog.findById(id).isEmpty) throw NotFoundException("Catalog service not found")
		catalog.deleteById(id)
	}
}
