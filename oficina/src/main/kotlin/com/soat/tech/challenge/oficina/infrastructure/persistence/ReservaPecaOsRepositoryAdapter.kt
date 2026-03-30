package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.ReservaPecaOs
import com.soat.tech.challenge.oficina.domain.model.StatusReservaPecaOs
import com.soat.tech.challenge.oficina.domain.port.ReservaPecaOsRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.OrdemServicoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PecaJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ReservaPecaOsJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ReservaPecaOsEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ReservaPecaOsRepositoryAdapter(
	private val reservaJpa: ReservaPecaOsJpaRepository,
	private val ordemJpa: OrdemServicoJpaRepository,
	private val pecaJpa: PecaJpaRepository,
) : ReservaPecaOsRepository {

	override fun sumPendingReservedQuantity(partId: UUID): Int =
		reservaJpa.sumQuantityByPartIdAndStatus(partId.toString(), StatusReservaPecaOs.PENDENTE.name)

	override fun sumPendingReservedQuantityExcludingWorkOrder(partId: UUID, excludeWorkOrderId: UUID): Int =
		reservaJpa.sumQuantityByPartIdAndStatusExcludingWorkOrder(
			partId.toString(),
			StatusReservaPecaOs.PENDENTE.name,
			excludeWorkOrderId.toString(),
		)

	override fun replacePendingReservations(workOrderId: UUID, partIdToQuantity: Map<UUID, Int>) {
		val wid = workOrderId.toString()
		reservaJpa.deleteByWorkOrder_IdAndStatus(wid, StatusReservaPecaOs.PENDENTE.name)
		val wo = ordemJpa.findById(wid).orElseThrow { IllegalArgumentException("Work order not found") }
		for ((pid, qty) in partIdToQuantity) {
			if (qty <= 0) continue
			val part = pecaJpa.findById(pid.toString()).orElseThrow { IllegalArgumentException("Part not found") }
			reservaJpa.save(
				ReservaPecaOsEntity(
					id = UUID.randomUUID().toString(),
					workOrder = wo,
					part = part,
					quantity = qty,
					status = StatusReservaPecaOs.PENDENTE.name,
				),
			)
		}
	}

	override fun cancelPendingForWorkOrder(workOrderId: UUID) {
		reservaJpa.deleteByWorkOrder_IdAndStatus(workOrderId.toString(), StatusReservaPecaOs.PENDENTE.name)
	}

	override fun findPendingByWorkOrder(workOrderId: UUID): List<ReservaPecaOs> =
		reservaJpa.findByWorkOrder_IdAndStatus(workOrderId.toString(), StatusReservaPecaOs.PENDENTE.name).map { e ->
			ReservaPecaOs(
				id = UUID.fromString(e.id),
				workOrderId = UUID.fromString(e.workOrder!!.id),
				partId = UUID.fromString(e.part!!.id),
				quantity = e.quantity,
				status = StatusReservaPecaOs.valueOf(e.status),
			)
		}

	override fun confirmPendingForWorkOrder(workOrderId: UUID) {
		val pending = reservaJpa.findByWorkOrder_IdAndStatus(
			workOrderId.toString(),
			StatusReservaPecaOs.PENDENTE.name,
		)
		for (row in pending) {
			val part = row.part!!
			val nextStock = part.stockQuantity - row.quantity
			require(nextStock >= 0) { "Estoque insuficiente para confirmar saída da reserva" }
			part.stockQuantity = nextStock
			pecaJpa.save(part)
			row.status = StatusReservaPecaOs.CONFIRMADA.name
			reservaJpa.save(row)
		}
	}
}
