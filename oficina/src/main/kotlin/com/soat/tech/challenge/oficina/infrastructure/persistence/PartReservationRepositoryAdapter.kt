package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.exception.InsufficientStockException
import com.soat.tech.challenge.oficina.domain.model.PartReservation
import com.soat.tech.challenge.oficina.domain.model.PartReservationStatus
import com.soat.tech.challenge.oficina.domain.port.PartReservationRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.WorkOrderJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PartJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PartReservationJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PartReservationEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PartReservationRepositoryAdapter(
	private val reservaJpa: PartReservationJpaRepository,
	private val ordemJpa: WorkOrderJpaRepository,
	private val pecaJpa: PartJpaRepository,
) : PartReservationRepository {

	override fun sumPendingReservedQuantity(partId: UUID): Int =
		reservaJpa.sumQuantityByPartIdAndStatus(partId.toString(), PartReservationStatus.PENDING.name)

	override fun sumPendingReservedQuantityExcludingWorkOrder(partId: UUID, excludeWorkOrderId: UUID): Int =
		reservaJpa.sumQuantityByPartIdAndStatusExcludingWorkOrder(
			partId.toString(),
			PartReservationStatus.PENDING.name,
			excludeWorkOrderId.toString(),
		)

	override fun replacePendingReservations(workOrderId: UUID, partIdToQuantity: Map<UUID, Int>) {
		val wid = workOrderId.toString()
		reservaJpa.deleteByWorkOrder_IdAndStatus(wid, PartReservationStatus.PENDING.name)
		val wo = ordemJpa.findById(wid).orElseThrow { IllegalArgumentException("Work order not found") }
		for ((pid, qty) in partIdToQuantity) {
			if (qty <= 0) continue
			val part = pecaJpa.findById(pid.toString()).orElseThrow { IllegalArgumentException("Part not found") }
			reservaJpa.save(
				PartReservationEntity(
					id = UUID.randomUUID().toString(),
					workOrder = wo,
					part = part,
					quantity = qty,
					status = PartReservationStatus.PENDING.name,
				),
			)
		}
	}

	override fun cancelPendingForWorkOrder(workOrderId: UUID) {
		reservaJpa.deleteByWorkOrder_IdAndStatus(workOrderId.toString(), PartReservationStatus.PENDING.name)
	}

	override fun findPendingByWorkOrder(workOrderId: UUID): List<PartReservation> =
		reservaJpa.findByWorkOrder_IdAndStatus(workOrderId.toString(), PartReservationStatus.PENDING.name).map { e ->
			PartReservation(
				id = UUID.fromString(e.id),
				workOrderId = UUID.fromString(e.workOrder!!.id),
				partId = UUID.fromString(e.part!!.id),
				quantity = e.quantity,
				status = PartReservationStatus.valueOf(e.status),
			)
		}

	override fun confirmPendingForWorkOrder(workOrderId: UUID) {
		val pending = reservaJpa.findByWorkOrder_IdAndStatus(
			workOrderId.toString(),
			PartReservationStatus.PENDING.name,
		)
		for (row in pending) {
			val part = row.part!!
			val nextStock = part.stockQuantity - row.quantity
			if (nextStock < 0) throw InsufficientStockException(part.id, row.quantity, part.stockQuantity)
			part.stockQuantity = nextStock
			pecaJpa.save(part)
			row.status = PartReservationStatus.CONFIRMED.name
			reservaJpa.save(row)
		}
	}
}
