package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.LowStockAlertResponse
import com.soat.tech.challenge.oficina.application.api.dto.PartReservationResponse
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.port.WorkOrderRepository
import com.soat.tech.challenge.oficina.domain.port.PartRepository
import com.soat.tech.challenge.oficina.domain.port.PartReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WarehouseApplicationService(
	private val reservations: PartReservationRepository,
	private val workOrders: WorkOrderRepository,
	private val parts: PartRepository,
) {

	@Transactional(readOnly = true)
	fun listPendingReservationsForWorkOrder(workOrderId: UUID): List<PartReservationResponse> {
		workOrders.findById(workOrderId).orElseThrow { NotFoundException("Work order not found") }
		return reservations.findPendingByWorkOrder(workOrderId).map { r ->
			val name = parts.findById(r.partId).map { it.name }.orElse("")
			PartReservationResponse(
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
	fun listLowStockAlerts(): List<LowStockAlertResponse> =
		parts.findAllAtOrBelowReplenishment().map { p ->
			val reserved = reservations.sumPendingReservedQuantity(p.id)
			LowStockAlertResponse(
				partId = p.id,
				code = p.code,
				name = p.name,
				stockQuantity = p.stockQuantity,
				replenishmentPoint = p.replenishmentPoint,
				pendingReservedQuantity = reserved,
			)
		}
}
