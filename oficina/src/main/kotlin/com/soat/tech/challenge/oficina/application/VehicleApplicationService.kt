package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.VehicleRequest
import com.soat.tech.challenge.oficina.application.api.dto.VehicleResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.BusinessRuleException
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.model.LicensePlate
import com.soat.tech.challenge.oficina.domain.model.Vehicle
import com.soat.tech.challenge.oficina.domain.port.CustomerRepository
import com.soat.tech.challenge.oficina.domain.port.VehicleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VehicleApplicationService(
	private val vehicles: VehicleRepository,
	private val customers: CustomerRepository,
) {

	@Transactional
	fun create(req: VehicleRequest): VehicleResponse {
		val customerId = req.customerId!!
		customers.findById(customerId).orElseThrow { NotFoundException("Customer not found") }
		val plate = LicensePlate.parse(req.plate)
		vehicles.findByLicensePlate(plate).ifPresent { throw BusinessRuleException("Plate already registered") }
		val v = Vehicle(
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
	fun list(): List<VehicleResponse> = vehicles.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): VehicleResponse =
		vehicles.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Vehicle not found") }

	@Transactional
	fun update(id: UUID, req: VehicleRequest): VehicleResponse {
		val existing = vehicles.findById(id).orElseThrow { NotFoundException("Vehicle not found") }
		val customerId = req.customerId!!
		customers.findById(customerId).orElseThrow { NotFoundException("Customer not found") }
		val plate = LicensePlate.parse(req.plate)
		vehicles.findByLicensePlate(plate).ifPresent { other ->
			if (other.id != id) throw BusinessRuleException("Plate already used by another vehicle")
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
