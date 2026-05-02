package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.LicensePlate
import com.soat.tech.challenge.oficina.domain.model.Vehicle
import com.soat.tech.challenge.oficina.domain.port.VehicleRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.CustomerJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.VehicleJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VehicleEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class VehicleRepositoryAdapter(
	private val jpa: VehicleJpaRepository,
	private val clienteJpa: CustomerJpaRepository,
) : VehicleRepository {

	override fun save(veiculo: Vehicle): Vehicle {
		val customer = clienteJpa.findById(veiculo.customerId.toString())
			.orElseThrow { IllegalArgumentException("Customer not found") }
		val e = VehicleEntity(
			id = veiculo.id.toString(),
			customer = customer,
			licensePlate = veiculo.licensePlate.normalized,
			brand = veiculo.brand,
			model = veiculo.model,
			year = veiculo.year,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Vehicle> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByLicensePlate(plate: LicensePlate): Optional<Vehicle> =
		Optional.ofNullable(jpa.findByLicensePlate(plate.normalized)).map { it.toDomain() }

	override fun findByCustomerId(customerId: UUID): List<Vehicle> =
		jpa.findByCustomer_Id(customerId.toString()).map { it.toDomain() }

	override fun findAll(): List<Vehicle> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
