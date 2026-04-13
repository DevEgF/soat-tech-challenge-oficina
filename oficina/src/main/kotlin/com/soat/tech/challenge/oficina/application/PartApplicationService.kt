package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.GoodsReceiptRequest
import com.soat.tech.challenge.oficina.application.api.dto.PartRequest
import com.soat.tech.challenge.oficina.application.api.dto.PartResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.BusinessRuleException
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.model.Part
import com.soat.tech.challenge.oficina.domain.port.PartRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PartApplicationService(
	private val parts: PartRepository,
) {

	@Transactional
	fun create(req: PartRequest): PartResponse {
		val code = req.code.trim()
		parts.findByCode(code).ifPresent { throw BusinessRuleException("Part code already exists") }
		val p = Part(
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
	fun list(): List<PartResponse> = parts.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): PartResponse =
		parts.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Part not found") }

	@Transactional
	fun update(id: UUID, req: PartRequest): PartResponse {
		val existing = parts.findById(id).orElseThrow { NotFoundException("Part not found") }
		val code = req.code.trim()
		parts.findByCode(code).ifPresent { other ->
			if (other.id != id) throw BusinessRuleException("Code already used by another part")
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
	fun recordGoodsReceipt(id: UUID, req: GoodsReceiptRequest): PartResponse {
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
