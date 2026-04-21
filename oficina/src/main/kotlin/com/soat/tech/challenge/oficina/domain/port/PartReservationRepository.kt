package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.PartReservation
import java.util.UUID

interface PartReservationRepository {

	/** Sum of quantities in PENDING reservations for this part (all work orders). */
	fun sumPendingReservedQuantity(partId: UUID): Int

	/** Pending reserved for this part excluding reservations tied to [excludeWorkOrderId]. */
	fun sumPendingReservedQuantityExcludingWorkOrder(partId: UUID, excludeWorkOrderId: UUID): Int

	/** Replace pending reservations for the work order with new lines (after validation). */
	fun replacePendingReservations(workOrderId: UUID, partIdToQuantity: Map<UUID, Int>)

	/** Cancel all PENDING reservations for the work order. */
	fun cancelPendingForWorkOrder(workOrderId: UUID)

	fun findPendingByWorkOrder(workOrderId: UUID): List<PartReservation>

	/** Mark pending as CONFIRMED (physical exit). Idempotent if none pending. */
	fun confirmPendingForWorkOrder(workOrderId: UUID)
}
