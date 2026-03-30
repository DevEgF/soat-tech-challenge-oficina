package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.VeiculoRequest
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VeiculoApplicationService(
	private val vehicles: VeiculoRepository,
	private val customers: ClienteRepository,
) {

	@Transactional
	fun create(req: VeiculoRequest): VeiculoResponse {
		val customerId = req.customerId!!
		customers.findById(customerId).orElseThrow { NotFoundException("Customer not found") }
		val plate = PlacaVeiculo.parse(req.plate)
		vehicles.findByLicensePlate(plate).ifPresent { throw IllegalArgumentException("Plate already registered") }
		val v = Veiculo(
			id = UUID.randomUUID(),
			customerId = customerId,
			licensePlate = plate,
			brand = req.brand.trim(),
			model = req.model.trim(),
			year = req.year!!,
		)
		return vehicles.save(v).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<VeiculoResponse> = vehicles.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): VeiculoResponse =
		vehicles.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Vehicle not found") }

	@Transactional
	fun update(id: UUID, req: VeiculoRequest): VeiculoResponse {
		val existing = vehicles.findById(id).orElseThrow { NotFoundException("Vehicle not found") }
		val customerId = req.customerId!!
		customers.findById(customerId).orElseThrow { NotFoundException("Customer not found") }
		val plate = PlacaVeiculo.parse(req.plate)
		vehicles.findByLicensePlate(plate).ifPresent { other ->
			if (other.id != id) throw IllegalArgumentException("Plate already used by another vehicle")
		}
		val updated = existing.copy(
			customerId = customerId,
			licensePlate = plate,
			brand = req.brand.trim(),
			model = req.model.trim(),
			year = req.year!!,
		)
		return vehicles.save(updated).toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		if (vehicles.findById(id).isEmpty) throw NotFoundException("Vehicle not found")
		vehicles.deleteById(id)
	}
}
