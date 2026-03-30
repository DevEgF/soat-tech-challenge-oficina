package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Work order (OrdemServico)")
class OrdemServicoTest {

	private val t0 = Instant.parse("2026-01-01T10:00:00Z")

	@Nested
	@DisplayName("Given a new work order")
	inner class GivenNewWorkOrder {

		@Test
		@DisplayName("when full swimlane happy path then status reaches ENTREGUE")
		fun happyPath() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(
					LinhaServicoOrdem(UUID.randomUUID(), 1, 5000),
				),
				partLines = emptyList(),
			)
			assertEquals(StatusOrdemServico.RECEBIDA, wo.status)
			wo.startDiagnosis(t0)
			assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, wo.status)
			wo.submitPlanForInternalApproval(t0.plusSeconds(60))
			assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO_INTERNA, wo.status)
			wo.approveInternal(t0.plusSeconds(90))
			assertNotNull(wo.internalApprovedAt)
			wo.sendQuoteToCustomer(t0.plusSeconds(120))
			assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, wo.status)
			wo.approveCustomerQuote(t0.plusSeconds(150))
			assertEquals(StatusOrdemServico.EM_EXECUCAO, wo.status)
			wo.completeServices(t0.plusSeconds(180))
			assertEquals(StatusOrdemServico.FINALIZADA, wo.status)
			wo.registerDelivery(t0.plusSeconds(240))
			assertEquals(StatusOrdemServico.ENTREGUE, wo.status)
		}

		@Test
		@DisplayName("when internal reject then CANCELADA")
		fun internalReject() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(LinhaServicoOrdem(UUID.randomUUID(), 1, 100)),
				partLines = emptyList(),
			)
			wo.startDiagnosis(t0)
			wo.submitPlanForInternalApproval(t0.plusSeconds(1))
			wo.rejectInternal(t0.plusSeconds(2))
			assertEquals(StatusOrdemServico.CANCELADA, wo.status)
			assertNotNull(wo.cancelledAt)
		}

		@Test
		@DisplayName("when customer rejects quote then CANCELADA")
		fun customerReject() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(LinhaServicoOrdem(UUID.randomUUID(), 1, 100)),
				partLines = emptyList(),
			)
			wo.startDiagnosis(t0)
			wo.submitPlanForInternalApproval(t0.plusSeconds(1))
			wo.approveInternal(t0.plusSeconds(2))
			wo.sendQuoteToCustomer(t0.plusSeconds(3))
			wo.rejectCustomerQuote(t0.plusSeconds(4))
			assertEquals(StatusOrdemServico.CANCELADA, wo.status)
		}
	}

	@Nested
	@DisplayName("Given work order in RECEBIDA")
	inner class GivenReceived {

		@Test
		@DisplayName("when submitPlan without diagnosis then throws")
		fun submitPlanWithoutDiagnosis() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			assertFailsWith<InvalidStatusTransitionException> {
				wo.submitPlanForInternalApproval(t0)
			}
		}
	}

	@Nested
	@DisplayName("Given AGUARDANDO_APROVACAO_INTERNA without internal approval")
	inner class GivenInternalPending {

		@Test
		@DisplayName("when sendQuoteToCustomer without approveInternal then throws")
		fun sendQuoteWithoutInternalApproval() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(LinhaServicoOrdem(UUID.randomUUID(), 1, 100)),
				partLines = emptyList(),
			)
			wo.startDiagnosis(t0)
			wo.submitPlanForInternalApproval(t0.plusSeconds(1))
			assertFailsWith<IllegalArgumentException> {
				wo.sendQuoteToCustomer(t0.plusSeconds(2))
			}
		}
	}

	@Nested
	@DisplayName("Given lines with quantities and prices")
	inner class GivenLines {

		@Test
		@DisplayName("when create then totals match line sums")
		fun totalsFromLines() {
			val sid = UUID.randomUUID()
			val pid = UUID.randomUUID()
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(LinhaServicoOrdem(sid, 2, 1000)),
				partLines = listOf(LinhaPecaOrdem(pid, 3, 500)),
			)
			assertEquals(2000L, wo.servicesTotalCents)
			assertEquals(1500L, wo.partsTotalCents)
			assertEquals(3500L, wo.totalCents)
		}
	}
}
