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
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
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
	private val clock: Clock,
) {

	private fun catalogServiceName(id: UUID): String? =
		catalogServices.findById(id).map { it.name }.orElse(null)

	private fun partName(id: UUID): String? =
		parts.findById(id).map { it.name }.orElse(null)

	private fun OrdemServico.toDto(): OrdemServicoResponse =
		toResponse({ catalogServiceName(it) }, { partName(it) })

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

	private fun now() = clock.instant()

	@Transactional
	fun startDiagnosis(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.startDiagnosis(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun sendQuote(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.sendQuote(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun approveQuote(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		deductStock(wo)
		wo.approveQuote(now())
		return workOrders.save(wo).toDto()
	}

	private fun deductStock(wo: OrdemServico) {
		for (line in wo.partLines) {
			val part = parts.findById(line.partId).orElseThrow { NotFoundException("Part not found") }
			if (part.stockQuantity < line.quantity) {
				throw InsufficientStockException(line.partId.toString(), line.quantity, part.stockQuantity)
			}
			parts.save(part.withAdjustedStock(-line.quantity))
		}
	}

	@Transactional
	fun returnToDiagnosis(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
		wo.returnToDiagnosis(now())
		return workOrders.save(wo).toDto()
	}

	@Transactional
	fun completeServices(id: UUID): OrdemServicoResponse {
		val wo = workOrders.findById(id).orElseThrow { NotFoundException("Work order not found") }
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
