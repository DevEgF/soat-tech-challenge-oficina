package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ClienteJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.VeiculoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VeiculoEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class VeiculoRepositoryAdapter(
	private val jpa: VeiculoJpaRepository,
	private val clienteJpa: ClienteJpaRepository,
) : VeiculoRepository {

	override fun save(veiculo: Veiculo): Veiculo {
		val customer = clienteJpa.findById(veiculo.customerId.toString())
			.orElseThrow { IllegalArgumentException("Customer not found") }
		val e = VeiculoEntity(
			id = veiculo.id.toString(),
			customer = customer,
			licensePlate = veiculo.licensePlate.normalized,
			brand = veiculo.brand,
			model = veiculo.model,
			year = veiculo.year,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Veiculo> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByLicensePlate(plate: PlacaVeiculo): Optional<Veiculo> =
		Optional.ofNullable(jpa.findByLicensePlate(plate.normalized)).map { it.toDomain() }

	override fun findByCustomerId(customerId: UUID): List<Veiculo> =
		jpa.findByCustomer_Id(customerId.toString()).map { it.toDomain() }

	override fun findAll(): List<Veiculo> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
