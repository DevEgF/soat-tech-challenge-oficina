package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaPecaRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaServicoRequest
import com.soat.tech.challenge.oficina.domain.exception.InsufficientStockException
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.ReservaPecaOs
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import com.soat.tech.challenge.oficina.domain.model.StatusReservaPecaOs
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.domain.port.ReservaPecaOsRepository
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrdemServicoApplicationServiceTest {

	private val workOrders = mockk<OrdemServicoRepository>()
	private val customers = mockk<ClienteRepository>()
	private val vehicles = mockk<VeiculoRepository>()
	private val catalogServices = mockk<ServicoCatalogoRepository>()
	private val parts = mockk<PecaRepository>()
	private val reservations = mockk<ReservaPecaOsRepository>()
	private val fixedInstant = Instant.parse("2026-03-01T12:00:00Z")
	private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
	private lateinit var service: OrdemServicoApplicationService

	@BeforeEach
	fun setup() {
		every { reservations.sumPendingReservedQuantityExcludingWorkOrder(any(), any()) } returns 0
		every { reservations.replacePendingReservations(any(), any()) } returns Unit
		every { reservations.cancelPendingForWorkOrder(any()) } returns Unit
		every { reservations.findPendingByWorkOrder(any()) } returns emptyList()
		every { reservations.confirmPendingForWorkOrder(any()) } returns Unit
		service = OrdemServicoApplicationService(
			workOrders,
			customers,
			vehicles,
			catalogServices,
			parts,
			reservations,
			clock,
		)
	}

	@Nested
	@DisplayName("Given no existing customer or vehicle")
	inner class GivenNewCustomerAndVehicle {

		@Test
		@DisplayName("when create then persists RECEBIDA work order")
		fun createNewOrder() {
			val doc = DocumentoFiscal.parse("52998224725")
			val sid = UUID.randomUUID()
			val pid = UUID.randomUUID()
			every { customers.findByFiscalDocument(doc) } returns Optional.empty()
			every { customers.save(any()) } answers { firstArg() }
			every { vehicles.findByLicensePlate(PlacaVeiculo.parse("ABC1234")) } returns Optional.empty()
			every { vehicles.save(any()) } answers { firstArg() }
			every { catalogServices.findById(sid) } returns Optional.of(
				ServicoCatalogo(sid, "Oleo", null, 1000, 30),
			)
			every { parts.findById(pid) } returns Optional.of(Peca(pid, "P1", "F", 500, 10))
			every { workOrders.save(any()) } answers { firstArg() }
			val req = CriarOrdemServicoRequest(
				customerTaxIdDigits = "52998224725",
				customerName = "Novo",
				plate = "ABC1234",
				vehicleBrand = "VW",
				vehicleModel = "Gol",
				vehicleYear = 2020,
				services = listOf(OrdemServicoLinhaServicoRequest(catalogServiceId = sid, quantity = 1)),
				parts = listOf(OrdemServicoLinhaPecaRequest(partId = pid, quantity = 1)),
			)
			val r = service.create(req)
			assertEquals(StatusOrdemServico.RECEBIDA, r.status)
			verify { workOrders.save(any()) }
		}
	}

	@Nested
	@DisplayName("Given saved work order in RECEBIDA")
	inner class GivenReceivedOrder {

		@Test
		@DisplayName("when startDiagnosis then status is EM_DIAGNOSTICO")
		fun startDiagnosis() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { workOrders.save(any()) } answers { firstArg() }
			val r = service.startDiagnosis(id)
			assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, r.status)
		}
	}

	@Nested
	@DisplayName("Given tracking lookup")
	inner class GivenTracking {

		@Test
		@DisplayName("when track with wrong document then throws")
		fun wrongDocument() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findByTrackingCode(wo.trackingCode) } returns Optional.of(wo)
			every { customers.findById(wo.customerId) } returns Optional.of(
				Cliente(wo.customerId, DocumentoFiscal.parse("52998224725"), "A"),
			)
			assertFailsWith<IllegalArgumentException> {
				service.track("39053344705", wo.trackingCode)
			}
		}

		@Test
		@DisplayName("when track with matching document then returns masked payload")
		fun success() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			val doc = DocumentoFiscal.parse("52998224725")
			every { workOrders.findByTrackingCode(wo.trackingCode) } returns Optional.of(wo)
			every { customers.findById(wo.customerId) } returns Optional.of(Cliente(wo.customerId, doc, "A"))
			every { vehicles.findById(wo.vehicleId) } returns Optional.of(
				Veiculo(wo.vehicleId, wo.customerId, PlacaVeiculo.parse("XYZ1234"), "F", "M", 2019),
			)
			val r = service.track("52998224725", wo.trackingCode)
			assertEquals(wo.trackingCode, r.trackingCode)
			assertEquals("XYZ1234", r.vehiclePlate)
		}
	}

	@Nested
	@DisplayName("Given order with part lines and stock")
	inner class GivenStock {

		@Test
		@DisplayName("when submit plan through customer approve then does not deduct stock immediately")
		fun planReserveAndCustomerApprove() {
			val id = UUID.randomUUID()
			val pid = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = mutableListOf(LinhaPecaOrdem(pid, 2, 100)),
			)
			wo.startDiagnosis(fixedInstant)
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { parts.findById(pid) } returns Optional.of(Peca(pid, "C", "N", 100, 5))
			every { workOrders.save(any()) } answers { firstArg() }
			service.submitPlanForInternalApproval(id)
			service.approveInternal(id)
			service.sendQuoteToCustomer(id)
			verify(exactly = 0) { parts.save(any()) }
			every { workOrders.findByTrackingCode(wo.trackingCode) } returns Optional.of(wo)
			every { customers.findById(wo.customerId) } returns Optional.of(
				Cliente(wo.customerId, DocumentoFiscal.parse("52998224725"), "X"),
			)
			every { vehicles.findById(wo.vehicleId) } returns Optional.of(
				Veiculo(wo.vehicleId, wo.customerId, PlacaVeiculo.parse("ABC1234"), "F", "M", 2020),
			)
			service.approveCustomerQuote("52998224725", wo.trackingCode)
			assertEquals(StatusOrdemServico.EM_EXECUCAO, wo.status)
			verify(exactly = 0) { parts.save(any()) }
		}

		@Test
		@DisplayName("when submit plan without enough available stock then throws")
		fun insufficientStock() {
			val id = UUID.randomUUID()
			val pid = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = mutableListOf(LinhaPecaOrdem(pid, 9, 100)),
			)
			wo.startDiagnosis(fixedInstant)
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { parts.findById(pid) } returns Optional.of(Peca(pid, "C", "N", 100, 2))
			assertFailsWith<InsufficientStockException> {
				service.submitPlanForInternalApproval(id)
			}
		}
	}

	@Nested
	@DisplayName("Given internal approval flow")
	inner class GivenInternal {

		@Test
		@DisplayName("when rejectInternal then cancels reservations and sets CANCELADA")
		fun rejectInternal() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			wo.startDiagnosis(fixedInstant)
			wo.submitPlanForInternalApproval(fixedInstant.plusSeconds(1))
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { workOrders.save(any()) } answers { firstArg() }
			service.rejectInternal(id)
			assertEquals(StatusOrdemServico.CANCELADA, wo.status)
			verify { reservations.cancelPendingForWorkOrder(id) }
		}
	}

	@Nested
	@DisplayName("Given customer quote actions")
	inner class GivenCustomerQuote {

		@Test
		@DisplayName("when rejectCustomerQuote then cancels reservations")
		fun rejectCustomer() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			wo.startDiagnosis(fixedInstant)
			wo.submitPlanForInternalApproval(fixedInstant.plusSeconds(1))
			wo.approveInternal(fixedInstant.plusSeconds(2))
			wo.sendQuoteToCustomer(fixedInstant.plusSeconds(3))
			every { workOrders.findByTrackingCode(wo.trackingCode) } returns Optional.of(wo)
			every { customers.findById(wo.customerId) } returns Optional.of(
				Cliente(wo.customerId, DocumentoFiscal.parse("52998224725"), "X"),
			)
			every { vehicles.findById(wo.vehicleId) } returns Optional.of(
				Veiculo(wo.vehicleId, wo.customerId, PlacaVeiculo.parse("ABC1234"), "F", "M", 2020),
			)
			every { workOrders.save(any()) } answers { firstArg() }
			service.rejectCustomerQuote("52998224725", wo.trackingCode)
			assertEquals(StatusOrdemServico.CANCELADA, wo.status)
			verify { reservations.cancelPendingForWorkOrder(id) }
		}

		@Test
		@DisplayName("when approveCustomerQuote in wrong status then throws")
		fun approveWrongStatus() {
			val wo = OrdemServico.create(
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findByTrackingCode(wo.trackingCode) } returns Optional.of(wo)
			every { customers.findById(wo.customerId) } returns Optional.of(
				Cliente(wo.customerId, DocumentoFiscal.parse("52998224725"), "X"),
			)
			assertFailsWith<IllegalArgumentException> {
				service.approveCustomerQuote("52998224725", wo.trackingCode)
			}
		}
	}

	@Nested
	@DisplayName("Given order in execution")
	inner class GivenInExecution {

		@Test
		@DisplayName("when completeServices with pending reservations then throws")
		fun completeBlockedByPending() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			).copy(
				status = StatusOrdemServico.EM_EXECUCAO,
				workStartedAt = fixedInstant,
				approvedAt = fixedInstant,
			)
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { reservations.findPendingByWorkOrder(id) } returns listOf(
				ReservaPecaOs(
					id = UUID.randomUUID(),
					workOrderId = id,
					partId = UUID.randomUUID(),
					quantity = 1,
					status = StatusReservaPecaOs.PENDENTE,
				),
			)
			assertFailsWith<IllegalStateException> {
				service.completeServices(id)
			}
		}

		@Test
		@DisplayName("when completeServices and registerDelivery then ENTREGUE")
		fun completeAndDeliver() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			).copy(
				status = StatusOrdemServico.EM_EXECUCAO,
				workStartedAt = fixedInstant,
				approvedAt = fixedInstant,
			)
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { workOrders.save(any()) } answers { firstArg() }
			service.completeServices(id)
			service.registerDelivery(id)
			assertEquals(StatusOrdemServico.ENTREGUE, wo.status)
		}
	}

	@Nested
	@DisplayName("Given list and get")
	inner class GivenQueries {

		@Test
		@DisplayName("when list and get then returns same id")
		fun listAndGet() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			every { workOrders.findAll() } returns listOf(wo)
			every { workOrders.findById(id) } returns Optional.of(wo)
			assertEquals(1, service.list().size)
			assertEquals(id, service.get(id).id)
		}

		@Test
		@DisplayName("when get unknown id then NotFoundException")
		fun getMissing() {
			val id = UUID.randomUUID()
			every { workOrders.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> {
				service.get(id)
			}
		}
	}

	@Nested
	@DisplayName("Given order awaiting customer approval")
	inner class GivenAwaitingApproval {

		@Test
		@DisplayName("when returnToDiagnosis then back to EM_DIAGNOSTICO")
		fun returnToDiagnosis() {
			val id = UUID.randomUUID()
			val wo = OrdemServico.create(
				id = id,
				customerId = UUID.randomUUID(),
				vehicleId = UUID.randomUUID(),
				serviceLines = emptyList(),
				partLines = emptyList(),
			)
			wo.startDiagnosis(fixedInstant)
			wo.submitPlanForInternalApproval(fixedInstant.plusSeconds(1))
			wo.approveInternal(fixedInstant.plusSeconds(2))
			wo.sendQuoteToCustomer(fixedInstant.plusSeconds(3))
			every { workOrders.findById(id) } returns Optional.of(wo)
			every { workOrders.save(any()) } answers { firstArg() }
			service.returnToDiagnosis(id)
			assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, wo.status)
			verify { reservations.cancelPendingForWorkOrder(id) }
		}
	}
}
