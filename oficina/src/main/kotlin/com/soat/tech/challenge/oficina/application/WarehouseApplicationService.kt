package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.EstoqueAlertaResponse
import com.soat.tech.challenge.oficina.application.api.dto.ReservaPecaOsResponse
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.domain.port.ReservaPecaOsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WarehouseApplicationService(
	private val reservations: ReservaPecaOsRepository,
	private val workOrders: OrdemServicoRepository,
	private val parts: PecaRepository,
) {

	@Transactional(readOnly = true)
	fun listPendingReservationsForWorkOrder(workOrderId: UUID): List<ReservaPecaOsResponse> {
		workOrders.findById(workOrderId).orElseThrow { NotFoundException("Work order not found") }
		return reservations.findPendingByWorkOrder(workOrderId).map { r ->
			val name = parts.findById(r.partId).map { it.name }.orElse("")
			ReservaPecaOsResponse(
				id = r.id,
				workOrderId = r.workOrderId,
				partId = r.partId,
				partName = name,
				quantity = r.quantity,
				status = r.status.name,
			)
		}
	}

	@Transactional
	fun confirmStockExitForWorkOrder(workOrderId: UUID) {
		workOrders.findById(workOrderId).orElseThrow { NotFoundException("Work order not found") }
		reservations.confirmPendingForWorkOrder(workOrderId)
	}

	@Transactional(readOnly = true)
	fun listLowStockAlerts(): List<EstoqueAlertaResponse> =
		parts.findAllAtOrBelowReplenishment().map { p ->
			val reserved = reservations.sumPendingReservedQuantity(p.id)
			EstoqueAlertaResponse(
				partId = p.id,
				code = p.code,
				name = p.name,
				stockQuantity = p.stockQuantity,
				replenishmentPoint = p.replenishmentPoint,
				pendingReservedQuantity = reserved,
			)
		}
}
