package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.model.WorkOrder
import com.soat.tech.challenge.oficina.domain.model.Part
import com.soat.tech.challenge.oficina.domain.model.PartReservation
import com.soat.tech.challenge.oficina.domain.model.PartReservationStatus
import com.soat.tech.challenge.oficina.domain.port.WorkOrderRepository
import com.soat.tech.challenge.oficina.domain.port.PartRepository
import com.soat.tech.challenge.oficina.domain.port.PartReservationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WarehouseApplicationServiceTest {

	private val reservations = mockk<PartReservationRepository>(relaxed = true)
	private val workOrders = mockk<WorkOrderRepository>()
	private val parts = mockk<PartRepository>()
	private val service = WarehouseApplicationService(reservations, workOrders, parts)

	@Nested
	@DisplayName("listPendingReservationsForWorkOrder")
	inner class ListPending {

		private val woId = UUID.randomUUID()
		private val partId = UUID.randomUUID()
		private val reservaId = UUID.randomUUID()

		@Test
		@DisplayName("when work order missing then NotFoundException")
		fun woNotFound() {
			every { workOrders.findById(woId) } returns Optional.empty()
			assertFailsWith<NotFoundException> {
				service.listPendingReservationsForWorkOrder(woId)
			}
		}

		@Test
		@DisplayName("when pending rows exist then maps part name")
		fun mapsRows() {
			val wo = WorkOrder.create(
				id = woId,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findById(woId) } returns Optional.of(wo)
			val row = PartReservation(
				id = reservaId,
				workOrderId = woId,
				partId = partId,
				quantity = 3,
				status = PartReservationStatus.PENDING,
			)
			every { reservations.findPendingByWorkOrder(woId) } returns listOf(row)
			every { parts.findById(partId) } returns Optional.of(
				Part(partId, "C", "NomePart", 100, 10, replenishmentPoint = 2),
			)
			val out = service.listPendingReservationsForWorkOrder(woId)
			assertEquals(1, out.size)
			assertEquals("NomePart", out[0].partName)
			assertEquals(3, out[0].quantity)
			assertEquals("PENDING", out[0].status)
		}

		@Test
		@DisplayName("when part missing then empty part name")
		fun partMissing() {
			val wo = WorkOrder.create(
				id = woId,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findById(woId) } returns Optional.of(wo)
			val row = PartReservation(
				id = reservaId,
				workOrderId = woId,
				partId = partId,
				quantity = 1,
				status = PartReservationStatus.PENDING,
			)
			every { reservations.findPendingByWorkOrder(woId) } returns listOf(row)
			every { parts.findById(partId) } returns Optional.empty()
			val out = service.listPendingReservationsForWorkOrder(woId)
			assertEquals("", out[0].partName)
		}
	}

	@Nested
	@DisplayName("confirmStockExitForWorkOrder")
	inner class Confirm {

		@Test
		@DisplayName("when work order missing then NotFoundException")
		fun woNotFound() {
			val id = UUID.randomUUID()
			every { workOrders.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> {
				service.confirmStockExitForWorkOrder(id)
			}
		}

		@Test
		@DisplayName("when ok then delegates to reservations")
		fun delegates() {
			val id = UUID.randomUUID()
			val wo = WorkOrder.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findById(id) } returns Optional.of(wo)
			service.confirmStockExitForWorkOrder(id)
			verify { reservations.confirmPendingForWorkOrder(id) }
		}
	}

	@Nested
	@DisplayName("listLowStockAlerts")
	inner class Alerts {

		@Test
		@DisplayName("when parts below replenishment then includes pending reserved sum")
		fun listsAlerts() {
			val pid = UUID.randomUUID()
			val p = Part(pid, "X", "Y", 50, 1, replenishmentPoint = 5)
			every { parts.findAllAtOrBelowReplenishment() } returns listOf(p)
			every { reservations.sumPendingReservedQuantity(pid) } returns 2
			val out = service.listLowStockAlerts()
			assertEquals(1, out.size)
			assertEquals(1, out[0].stockQuantity)
			assertEquals(5, out[0].replenishmentPoint)
			assertEquals(2, out[0].pendingReservedQuantity)
		}

		@Test
		@DisplayName("when none below threshold then empty list")
		fun empty() {
			every { parts.findAllAtOrBelowReplenishment() } returns emptyList()
			assertEquals(0, service.listLowStockAlerts().size)
		}
	}
}
