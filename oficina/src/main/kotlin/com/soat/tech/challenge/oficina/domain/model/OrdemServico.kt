package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
import java.time.Instant
import java.util.UUID

data class LinhaServicoOrdem(
	val catalogServiceId: UUID,
	val quantity: Int,
	val unitPriceCents: Long,
) {
	init {
		require(quantity > 0) { "Service quantity must be positive" }
		require(unitPriceCents >= 0) { "Price cannot be negative" }
	}
}

data class LinhaPecaOrdem(
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
 */
data class OrdemServico(
	val id: UUID,
	val trackingCode: String,
	val customerId: UUID,
	val vehicleId: UUID,
	var status: StatusOrdemServico,
	val serviceLines: MutableList<LinhaServicoOrdem>,
	val partLines: MutableList<LinhaPecaOrdem>,
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
) {

	fun recalculateQuote() {
		servicesTotalCents = serviceLines.sumOf { it.quantity.toLong() * it.unitPriceCents }
		partsTotalCents = partLines.sumOf { it.quantity.toLong() * it.unitPriceCents }
		totalCents = servicesTotalCents + partsTotalCents
	}

	private fun requireStatus(expected: StatusOrdemServico, action: String) {
		if (status != expected) {
			throw InvalidStatusTransitionException(status.name, action)
		}
	}

	fun startDiagnosis(now: Instant) {
		requireStatus(StatusOrdemServico.RECEBIDA, "startDiagnosis")
		status = StatusOrdemServico.EM_DIAGNOSTICO
		diagnosedAt = now
	}

	/** Technician: end diagnosis, submit plan and trigger stock reservation. */
	fun submitPlanForInternalApproval(now: Instant) {
		requireStatus(StatusOrdemServico.EM_DIAGNOSTICO, "submitPlanForInternalApproval")
		recalculateQuote()
		status = StatusOrdemServico.AGUARDANDO_APROVACAO_INTERNA
		planSubmittedAt = now
		internalApprovedAt = null
	}

	/** Administrator: reject internal plan. */
	fun rejectInternal(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO_INTERNA, "rejectInternal")
		status = StatusOrdemServico.CANCELADA
		cancelledAt = now
	}

	/** Administrator: approve internal execution (attendant may send quote to customer). */
	fun approveInternal(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO_INTERNA, "approveInternal")
		internalApprovedAt = now
	}

	/** Attendant: send quote to customer (after internal approval). */
	fun sendQuoteToCustomer(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO_INTERNA, "sendQuoteToCustomer")
		require(internalApprovedAt != null) { "Internal approval required before sending quote to customer" }
		recalculateQuote()
		status = StatusOrdemServico.AGUARDANDO_APROVACAO
		quoteSentAt = now
	}

	/** Customer approves quote (public API). */
	fun approveCustomerQuote(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "approveCustomerQuote")
		status = StatusOrdemServico.EM_EXECUCAO
		approvedAt = now
		workStartedAt = now
	}

	/** Customer rejects quote (public API). */
	fun rejectCustomerQuote(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "rejectCustomerQuote")
		status = StatusOrdemServico.CANCELADA
		cancelledAt = now
	}

	fun returnToDiagnosis(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "returnToDiagnosis")
		status = StatusOrdemServico.EM_DIAGNOSTICO
		quoteSentAt = null
		internalApprovedAt = null
		planSubmittedAt = null
	}

	fun completeServices(now: Instant) {
		requireStatus(StatusOrdemServico.EM_EXECUCAO, "completeServices")
		status = StatusOrdemServico.FINALIZADA
		completedAt = now
	}

	fun registerDelivery(now: Instant) {
		requireStatus(StatusOrdemServico.FINALIZADA, "registerDelivery")
		status = StatusOrdemServico.ENTREGUE
		deliveredAt = now
	}

	companion object {
		fun create(
			id: UUID = UUID.randomUUID(),
			trackingCode: String = UUID.randomUUID().toString(),
			customerId: UUID,
			vehicleId: UUID,
			serviceLines: List<LinhaServicoOrdem>,
			partLines: List<LinhaPecaOrdem>,
		): OrdemServico {
			val wo = OrdemServico(
				id = id,
				trackingCode = trackingCode,
				customerId = customerId,
				vehicleId = vehicleId,
				status = StatusOrdemServico.RECEBIDA,
				serviceLines = serviceLines.toMutableList(),
				partLines = partLines.toMutableList(),
				servicesTotalCents = 0,
				partsTotalCents = 0,
				totalCents = 0,
			)
			wo.recalculateQuote()
			return wo
		}
	}
}
