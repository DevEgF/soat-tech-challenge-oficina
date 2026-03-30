package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.ReservaPecaOs
import java.util.UUID

interface ReservaPecaOsRepository {

	/** Sum of quantities in PENDENTE reservations for this part (all work orders). */
	fun sumPendingReservedQuantity(partId: UUID): Int

	/** Pending reserved for this part excluding reservations tied to [excludeWorkOrderId]. */
	fun sumPendingReservedQuantityExcludingWorkOrder(partId: UUID, excludeWorkOrderId: UUID): Int

	/** Replace pending reservations for the work order with new lines (after validation). */
	fun replacePendingReservations(workOrderId: UUID, partIdToQuantity: Map<UUID, Int>)

	/** Cancel all PENDENTE reservations for the work order. */
	fun cancelPendingForWorkOrder(workOrderId: UUID)

	fun findPendingByWorkOrder(workOrderId: UUID): List<ReservaPecaOs>

	/** Mark pending as CONFIRMADA (physical exit). Idempotent if none pending. */
	fun confirmPendingForWorkOrder(workOrderId: UUID)
}
