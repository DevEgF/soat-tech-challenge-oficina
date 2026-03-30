package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.EntradaMercadoriaRequest
import com.soat.tech.challenge.oficina.application.api.dto.PecaRequest
import com.soat.tech.challenge.oficina.application.api.dto.PecaResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PecaApplicationService(
	private val parts: PecaRepository,
) {

	@Transactional
	fun create(req: PecaRequest): PecaResponse {
		val code = req.code.trim()
		parts.findByCode(code).ifPresent { throw IllegalArgumentException("Part code already exists") }
		val p = Peca(
			id = UUID.randomUUID(),
			code = code,
			name = req.name.trim(),
			priceCents = req.priceCents!!,
			stockQuantity = req.stockQuantity!!,
			replenishmentPoint = req.replenishmentPoint,
		)
		return parts.save(p).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<PecaResponse> = parts.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): PecaResponse =
		parts.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Part not found") }

	@Transactional
	fun update(id: UUID, req: PecaRequest): PecaResponse {
		val existing = parts.findById(id).orElseThrow { NotFoundException("Part not found") }
		val code = req.code.trim()
		parts.findByCode(code).ifPresent { other ->
			if (other.id != id) throw IllegalArgumentException("Code already used by another part")
		}
		val updated = existing.copy(
			code = code,
			name = req.name.trim(),
			priceCents = req.priceCents!!,
			stockQuantity = req.stockQuantity!!,
			replenishmentPoint = req.replenishmentPoint,
		)
		return parts.save(updated).toResponse()
	}

	@Transactional
	fun recordGoodsReceipt(id: UUID, req: EntradaMercadoriaRequest): PecaResponse {
		val existing = parts.findById(id).orElseThrow { NotFoundException("Part not found") }
		val qty = req.quantity!!
		val updated = existing.withAdjustedStock(qty)
		return parts.save(updated).toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		if (parts.findById(id).isEmpty) throw NotFoundException("Part not found")
		parts.deleteById(id)
	}
}
