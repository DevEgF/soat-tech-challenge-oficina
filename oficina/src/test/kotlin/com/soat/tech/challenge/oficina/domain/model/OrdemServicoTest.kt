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

@DisplayName("Work order (WorkOrder)")
class WorkOrderTest {

	private val t0 = Instant.parse("2026-01-01T10:00:00Z")

	@Nested
	@DisplayName("Given a new work order")
	inner class GivenNewWorkOrder {

		@Test
		@DisplayName("when full swimlane happy path then status reaches DELIVERED")
		fun happyPath() {
			val wo = WorkOrder.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(
					ServiceLine(UUID.randomUUID(), 1, 5000),
				),
				partLines = emptyList(),
			)
			assertEquals(WorkOrderStatus.RECEIVED, wo.status)
			wo.startDiagnosis(t0)
			assertEquals(WorkOrderStatus.IN_DIAGNOSIS, wo.status)
			wo.submitPlanForInternalApproval(t0.plusSeconds(60))
			assertEquals(WorkOrderStatus.PENDING_INTERNAL_APPROVAL, wo.status)
			wo.approveInternal(t0.plusSeconds(90))
			assertNotNull(wo.internalApprovedAt)
			wo.sendQuoteToCustomer(t0.plusSeconds(120))
			assertEquals(WorkOrderStatus.PENDING_APPROVAL, wo.status)
			wo.approveCustomerQuote(t0.plusSeconds(150))
			assertEquals(WorkOrderStatus.IN_EXECUTION, wo.status)
			wo.completeServices(t0.plusSeconds(180))
			assertEquals(WorkOrderStatus.FINALIZED, wo.status)
			wo.registerDelivery(t0.plusSeconds(240))
			assertEquals(WorkOrderStatus.DELIVERED, wo.status)
		}

		@Test
		@DisplayName("when internal reject then CANCELLED")
		fun internalReject() {
			val wo = WorkOrder.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(ServiceLine(UUID.randomUUID(), 1, 100)),
				partLines = emptyList(),
			)
			wo.startDiagnosis(t0)
			wo.submitPlanForInternalApproval(t0.plusSeconds(1))
			wo.rejectInternal(t0.plusSeconds(2))
			assertEquals(WorkOrderStatus.CANCELLED, wo.status)
			assertNotNull(wo.cancelledAt)
		}

		@Test
		@DisplayName("when customer rejects quote then CANCELLED")
		fun customerReject() {
			val wo = WorkOrder.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(ServiceLine(UUID.randomUUID(), 1, 100)),
				partLines = emptyList(),
			)
			wo.startDiagnosis(t0)
			wo.submitPlanForInternalApproval(t0.plusSeconds(1))
			wo.approveInternal(t0.plusSeconds(2))
			wo.sendQuoteToCustomer(t0.plusSeconds(3))
			wo.rejectCustomerQuote(t0.plusSeconds(4))
			assertEquals(WorkOrderStatus.CANCELLED, wo.status)
		}
	}

	@Nested
	@DisplayName("Given work order in RECEIVED")
	inner class GivenReceived {

		@Test
		@DisplayName("when submitPlan without diagnosis then throws")
		fun submitPlanWithoutDiagnosis() {
			val wo = WorkOrder.create(
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
	@DisplayName("Given PENDING_INTERNAL_APPROVAL without internal approval")
	inner class GivenInternalPending {

		@Test
		@DisplayName("when sendQuoteToCustomer without approveInternal then throws")
		fun sendQuoteWithoutInternalApproval() {
			val wo = WorkOrder.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(ServiceLine(UUID.randomUUID(), 1, 100)),
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
			val wo = WorkOrder.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = listOf(ServiceLine(sid, 2, 1000)),
				partLines = listOf(PartLine(pid, 3, 500)),
			)
			assertEquals(2000L, wo.servicesTotalCents)
			assertEquals(1500L, wo.partsTotalCents)
			assertEquals(3500L, wo.totalCents)
		}
	}
}
