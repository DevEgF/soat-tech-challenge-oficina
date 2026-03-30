package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ServicoCatalogoApplicationService(
	private val catalog: ServicoCatalogoRepository,
) {

	@Transactional
	fun create(req: ServicoCatalogoRequest): ServicoCatalogoResponse {
		val s = ServicoCatalogo(
			id = UUID.randomUUID(),
			name = req.name.trim(),
			description = req.description?.trim()?.takeIf { it.isNotEmpty() },
			priceCents = req.priceCents!!,
			estimatedMinutes = req.estimatedMinutes!!,
		)
		return catalog.save(s).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<ServicoCatalogoResponse> = catalog.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): ServicoCatalogoResponse =
		catalog.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Catalog service not found") }

	@Transactional
	fun update(id: UUID, req: ServicoCatalogoRequest): ServicoCatalogoResponse {
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
