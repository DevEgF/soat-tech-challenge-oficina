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
 * Work order (aggregate): quote derived from lines; explicit state machine.
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
	var quoteSentAt: Instant? = null,
	var approvedAt: Instant? = null,
	var workStartedAt: Instant? = null,
	var completedAt: Instant? = null,
	var deliveredAt: Instant? = null,
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

	fun sendQuote(now: Instant) {
		requireStatus(StatusOrdemServico.EM_DIAGNOSTICO, "sendQuote")
		recalculateQuote()
		status = StatusOrdemServico.AGUARDANDO_APROVACAO
		quoteSentAt = now
	}

	fun approveQuote(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "approveQuote")
		status = StatusOrdemServico.EM_EXECUCAO
		approvedAt = now
		workStartedAt = now
	}

	fun returnToDiagnosis(now: Instant) {
		requireStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "returnToDiagnosis")
		status = StatusOrdemServico.EM_DIAGNOSTICO
		quoteSentAt = null
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
