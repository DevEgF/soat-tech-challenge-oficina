package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.AcompanhamentoOsResponse
import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.InsufficientStockException
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.LinhaServicoOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.domain.port.ReservaPecaOsRepository
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class OrdemServicoApplicationService(
	private val workOrders: OrdemServicoRepository,
	private val customers: ClienteRepository,
	private val vehicles: VeiculoRepository,
	private val catalogServices: ServicoCatalogoRepository,
	private val parts: PecaRepository,
	private val reservations: ReservaPecaOsRepository,
	private val clock: Clock,
) {

	private fun catalogServiceName(id: UUID): String? =
		catalogServices.findById(id).map { it.name }.orElse(null)

	private fun partName(id: UUID): String? =
		parts.findById(id).map { it.name }.orElse(null)

	private fun OrdemServico.toDto(): OrdemServicoResponse =
		toResponse({ catalogServiceName(it) }, { partName(it) })

	private fun now() = clock.instant()

	private fun validateAndReplaceReservations(wo: OrdemServico) {
		val map = wo.partLines.associate { it.partId to it.quantity }
		for ((partId, need) in map) {
			val part = parts.findById(partId).orElseThrow { NotFoundException("Part not found") }
			val reservedElsewhere = reservations.sumPendingReservedQuantityExcludingWorkOrder(partId, wo.id)
			val available = part.stockQuantity - reservedElsewhere
			if (need > available) {
				throw InsufficientStockException(partId.toString(), need, available)
			}
		}
		reservations.replacePendingReservations(wo.id, map)
	}

	private fun cancelReservationsIfAny(woId: UUID) {
		reservations.cancelPendingForWorkOrder(woId)
	}

	@Transactional
	fun create(req: CriarOrdemServicoRequest): OrdemServicoResponse {
		val doc = DocumentoFiscal.parse(req.customerTaxIdDigits)
		val customer = customers.findByFiscalDocument(doc).orElseGet {
			customers.save(
				Cliente(
					id = UUID.randomUUID(),
					fiscalDocument = doc,
					name = req.customerName.trim(),
					email = req.customerEmail?.trim()?.takeIf { it.isNotEmpty() },
					phone = req.customerPhone?.trim()?.takeIf { it.isNotEmpty() },
				),
			)
		}
		val plate = PlacaVeiculo.parse(req.plate)
		val vehicle = vehicles.findByLicensePlate(plate).orElseGet {
			vehicles.save(
				Veiculo(
					id = UUID.randomUUID(),
					customerId = customer.id,
					licensePlate = plate,
					brand = req.vehicleBrand.trim(),
					model = req.vehicleModel.trim(),
					year = req.vehicleYear!!,
				),
			)
		}
		if (vehicle.customerId != customer.id) {
			throw IllegalArgumentException("Vehicle belongs to another customer")
		}
		val serviceLines = req.services.map { s ->
			val cat = catalogServices.findById(s.catalogServiceId!!)
				.orElseThrow { NotFoundException("Catalog service not found") }
			LinhaServicoOrdem(
				catalogServiceId = cat.id,
				quantity = s.quantity!!,
				unitPriceCents = cat.priceCents,
			)
		}
		val partLines = req.parts.map { p ->
			val part = parts.findById(p.partId!!)
				.orElseThrow { NotFoundException("Part not found") }
			LinhaPecaOrdem(
				partId = part.id,
				quantity = p.quantity!!,
				unitPriceCents = part.priceCents,
			)
		}
		val wo = OrdemServico.create(
			customerId = customer.id,
			vehicleId = vehicle.id,
			serviceLines = serviceLines,
			partLines = partLines,
		)
		return workOrders.save(wo).toDto()
	}

	@Transactional(readOnly = true)
	fun list(): List<OrdemServicoResponse> = workOrders.findAll().map { it.toDto() }

	@Transactional(readOnly = true)
	fun get(id: UUID): OrdemServicoResponse =
		workOrders.findById(id).map { it.toDto() }.orElseThrow { NotFoundException("Work order not found") }

	@Transactional(readOnly = true)
	fun track(customerTaxIdDigits: String, trackingCode: String): AcompanhamentoOsResponse {
		val doc = DocumentoFiscal.parse(customerTaxIdDigits)
		val wo = workOrders.findByTrackingCode(trackingCode.trim())
			.orElseThrow { NotFoundException("Work order not found") }
		val customer = customers.findById(wo.customerId).orElseThrow { NotFoundException("Customer not found") }
		if (customer.fiscalDocument.digits != doc.digits) {
			throw IllegalArgumentException("Document does not match this work order")
		}
		val vehicle = vehicles.findById(wo.vehicleId).orElseThrow { NotFoundException("Vehicle not found") }
		return AcompanhamentoOsResponse(
			trackingCode = wo.trackingCode,
			status = wo.status,
			totalCents = wo.totalCents,
			vehiclePlate = vehicle.licensePlate.normalized,
			maskedCustomerTaxId = maskTaxId(doc.digits),
		)
	}

	private fun maskTaxId(d: String): String = when (d.length) {
		11 -> "***.${d.take(3)}.${d.substring(3, 6)}-**"
		14 -> "**.${d.substring(2, 5)}.${d.substring(5, 8)}/****-**"
		else -> "***"
	}

	private fun loadWorkOrderForCustomer(documentDigits: String, trackingCode: String): OrdemServico {
		val doc = DocumentoFiscal.parse(documentDigits)
		val wo = workOrders.findByTrackingCode(trackingCode.trim())
			.orElseThrow { NotFoundException("Work order not found") }
		val customer = customers.findById(wo.customerId).orElseThrow { NotFoundException("Customer not found") }
		if (customer.fiscalDocument.digits != doc.digits) {
			throw IllegalArgumentException("Document does not match this work order")
		}
		return wo
	}

	@Transactional
	fun approveCustomerQuote(customerTaxIdDigits: String, trackingCode: String): AcompanhamentoOsResponse {
		val wo = loadWorkOrderForCustomer(customerTaxIdDigits, trackingCode)
		require(wo.status == StatusOrdemServico.AGUARDANDO_APROVACAO) {
			"Orçamento não está aguardando aprovação do cliente"
		}
		wo.approveCustomerQuote(now())
		workOrders.save(wo)
		return track(customerTaxIdDigits, trackingCode)
	}

	@Transactional
	fun rejectCustomerQuote(customerTaxIdDigits: String, trackingCode: String): AcompanhamentoOsResponse {
		val wo = loadWorkOrderForCustomer(customerTaxIdDigits, trackingCode)
		require(wo.status == StatusOrdemServico.AGUARDANDO_APROVACAO) {
			"Orçamento não está aguardando aprovação do cliente"
		}
		cancelReservationsIfAny(wo.id)
		wo.rejectCustomerQuote(now())
		workOrders.save(wo)
		return track(customerTaxIdDigits, trackingCode)
	}

	@Transactional
	fun startDiagnosis(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.startDiagnosis(now())
		return workOrders.save(wo).toDto()
	}

	/** Técnico: submete plano e cria reservas de peças. */
	@Transactional
	fun submitPlanForInternalApproval(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.submitPlanForInternalApproval(now())
		validateAndReplaceReservations(wo)
		return workOrders.save(wo).toDto()
	}

	/** Administrador: reprova plano interno. */
	@Transactional
	fun rejectInternal(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		cancelReservationsIfAny(wo.id)
		wo.rejectInternal(now())
		return workOrders.save(wo).toDto()
	}

	/** Administrador: aprova execução interna. */
	@Transactional
	fun approveInternal(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.approveInternal(now())
		return workOrders.save(wo).toDto()
	}

	/** Atendente: envia orçamento ao cliente. */
	@Transactional
	fun sendQuoteToCustomer(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.sendQuoteToCustomer(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun returnToDiagnosis(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		cancelReservationsIfAny(wo.id)
		wo.returnToDiagnosis(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun completeServices(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		val pending = reservations.findPendingByWorkOrder(wo.id)
		if (pending.isNotEmpty()) {
			throw IllegalStateException(
				"Existem reservas de peças pendentes de confirmação de saída pelo almoxarife",
			)
		}
		wo.completeServices(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun registerDelivery(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.registerDelivery(now())
		return workOrders.save(wo).toDto()
	}
}
