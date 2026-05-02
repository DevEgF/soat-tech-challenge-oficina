package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.LicensePlate
import com.soat.tech.challenge.oficina.domain.model.Vehicle
import java.util.Optional
import java.util.UUID

interface VehicleRepository {
	fun save(veiculo: Vehicle): Vehicle
	fun findById(id: UUID): Optional<Vehicle>
	fun findByLicensePlate(plate: LicensePlate): Optional<Vehicle>
	fun findByCustomerId(customerId: UUID): List<Vehicle>
	fun findAll(): List<Vehicle>
	fun deleteById(id: UUID)
}
