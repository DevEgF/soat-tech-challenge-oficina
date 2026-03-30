package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
		@DisplayName("when full happy path then status reaches ENTREGUE")
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
			wo.sendQuote(t0.plusSeconds(60))
			assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, wo.status)
			wo.approveQuote(t0.plusSeconds(120))
			assertEquals(StatusOrdemServico.EM_EXECUCAO, wo.status)
			wo.completeServices(t0.plusSeconds(180))
			assertEquals(StatusOrdemServico.FINALIZADA, wo.status)
			wo.registerDelivery(t0.plusSeconds(240))
			assertEquals(StatusOrdemServico.ENTREGUE, wo.status)
		}
	}

	@Nested
	@DisplayName("Given work order in RECEBIDA")
	inner class GivenReceived {

		@Test
		@DisplayName("when sendQuote without diagnosis then throws")
		fun sendQuoteWithoutDiagnosis() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			assertFailsWith<InvalidStatusTransitionException> {
				wo.sendQuote(t0)
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
