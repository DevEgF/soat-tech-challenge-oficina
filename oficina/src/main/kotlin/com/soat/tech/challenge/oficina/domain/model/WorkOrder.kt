package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
import java.time.Instant
import java.util.UUID

data class ServiceLine(
	val catalogServiceId: UUID,
	val quantity: Int,
	val unitPriceCents: Long,
) {
	init {
		require(quantity > 0) { "Service quantity must be positive" }
		require(unitPriceCents >= 0) { "Price cannot be negative" }
	}
}

data class PartLine(
	val partId: UUID,
	val quantity: Int,
	val unitPriceCents: Long,
) {
	init {
		require(quantity > 0) { "Part quantity must be positive" }
		require(unitPriceCents >= 0) { "Price cannot be negative" }
	}
}

/**
 * Work order (aggregate): quote derived from lines; explicit state machine (swimlane).
 * Collections are exposed as immutable List to preserve aggregate invariants.
 */
data class WorkOrder(
	val id: UUID,
	val trackingCode: String,
	val customerId: UUID,
	val vehicleId: UUID,
	var diagnosisNotes: String? = null,
	var status: WorkOrderStatus,
	var serviceLines: List<ServiceLine>,
	var partLines: List<PartLine>,
	var servicesTotalCents: Long,
	var partsTotalCents: Long,
	var totalCents: Long,
	var diagnosedAt: Instant? = null,
	/** Technician submitted plan; reservations created in application layer. */
	var planSubmittedAt: Instant? = null,
	/** Administrator approved internal execution. */
	var internalApprovedAt: Instant? = null,
	var quoteSentAt: Instant? = null,
	/** Customer approved quote (starts execution / monitoring). */
	var approvedAt: Instant? = null,
	var workStartedAt: Instant? = null,
	var completedAt: Instant? = null,
	var deliveredAt: Instant? = null,
	var cancelledAt: Instant? = null,
	/** Attendant reverted OS from PENDING_APPROVAL back to IN_DIAGNOSIS. */
	var returnedToDiagnosisAt: Instant? = null,
	/** Creation timestamp used for FIFO ordering in the work order listing. */
	var createdAt: Instant = Instant.now(),
) {

	fun recalculateQuote() {
		servicesTotalCents = serviceLines.sumOf { it.quantity.toLong() * it.unitPriceCents }
		partsTotalCents = partLines.sumOf { it.quantity.toLong() * it.unitPriceCents }
		totalCents = servicesTotalCents + partsTotalCents
	}

	private fun requireStatus(expected: WorkOrderStatus, action: String) {
		if (status != expected) {
			throw InvalidStatusTransitionException(status.name, action)
		}
	}

	fun startDiagnosis(now: Instant) {
		requireStatus(WorkOrderStatus.RECEIVED, "startDiagnosis")
		status = WorkOrderStatus.IN_DIAGNOSIS
		diagnosedAt = now
	}

	fun updateDiagnosisPlan(
		services: List<ServiceLine>,
		parts: List<PartLine>,
		notes: String?,
	) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "updateDiagnosisPlan")
		serviceLines = services
		partLines = parts
		diagnosisNotes = notes?.trim()?.takeIf { it.isNotEmpty() }
		recalculateQuote()
	}

	/** Technician: end diagnosis, submit plan and trigger stock reservation. */
	fun submitPlanForInternalApproval(now: Instant) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "submitPlanForInternalApproval")
		recalculateQuote()
		status = WorkOrderStatus.PENDING_INTERNAL_APPROVAL
		planSubmittedAt = now
		internalApprovedAt = null
	}

	/** Administrator: reject internal plan. */
	fun rejectInternal(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_INTERNAL_APPROVAL, "rejectInternal")
		status = WorkOrderStatus.CANCELLED
		cancelledAt = now
	}

	/** Administrator: approve internal review and release to customer approval. */
	fun approveInternal(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_INTERNAL_APPROVAL, "approveInternal")
		recalculateQuote()
		internalApprovedAt = now
		status = WorkOrderStatus.PENDING_APPROVAL
		quoteSentAt = now
	}

	/** Attendant: send quote to customer (after internal approval). */
	fun sendQuoteToCustomer(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_INTERNAL_APPROVAL, "sendQuoteToCustomer")
		require(internalApprovedAt != null) { "Internal approval required before sending quote to customer" }
		recalculateQuote()
		status = WorkOrderStatus.PENDING_APPROVAL
		quoteSentAt = now
	}

	/** Customer approves quote (public API). Transitions to AWAITING_PARTS_RELEASE; execution starts only after warehouse confirmation. */
	fun approveCustomerQuote(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_APPROVAL, "approveCustomerQuote")
		status = WorkOrderStatus.AWAITING_PARTS_RELEASE
		approvedAt = now
	}

	/** Policy: triggered by warehouse confirmation of stock exit. */
	fun startExecution(now: Instant) {
		requireStatus(WorkOrderStatus.AWAITING_PARTS_RELEASE, "startExecution")
		status = WorkOrderStatus.IN_EXECUTION
		workStartedAt = now
	}

	/** Customer rejects quote (public API). */
	fun rejectCustomerQuote(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_APPROVAL, "rejectCustomerQuote")
		status = WorkOrderStatus.CANCELLED
		cancelledAt = now
	}

	fun returnToDiagnosis(now: Instant) {
		requireStatus(WorkOrderStatus.PENDING_APPROVAL, "returnToDiagnosis")
		status = WorkOrderStatus.IN_DIAGNOSIS
		quoteSentAt = null
		internalApprovedAt = null
		planSubmittedAt = null
		returnedToDiagnosisAt = now
	}

	fun completeServices(now: Instant) {
		requireStatus(WorkOrderStatus.IN_EXECUTION, "completeServices")
		status = WorkOrderStatus.FINALIZED
		completedAt = now
	}

	fun registerDelivery(now: Instant) {
		requireStatus(WorkOrderStatus.FINALIZED, "registerDelivery")
		status = WorkOrderStatus.DELIVERED
		deliveredAt = now
	}

	companion object {
		fun create(
			id: UUID = UUID.randomUUID(),
			trackingCode: String = UUID.randomUUID().toString(),
			customerId: UUID,
			vehicleId: UUID,
			serviceLines: List<ServiceLine>,
			partLines: List<PartLine>,
		): WorkOrder {
			val wo = WorkOrder(
				id = id,
				trackingCode = trackingCode,
				customerId = customerId,
				vehicleId = vehicleId,
				diagnosisNotes = null,
				status = WorkOrderStatus.RECEIVED,
				serviceLines = serviceLines,
				partLines = partLines,
				servicesTotalCents = 0,
				partsTotalCents = 0,
				totalCents = 0,
			)
			wo.recalculateQuote()
			return wo
		}
	}
}
