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
		val cliente = clienteJpa.findById(veiculo.clienteId.toString())
			.orElseThrow { IllegalArgumentException("Cliente não encontrado") }
		val e = VeiculoEntity(
			id = veiculo.id.toString(),
			cliente = cliente,
			placa = veiculo.placa.normalizada,
			marca = veiculo.marca,
			modelo = veiculo.modelo,
			ano = veiculo.ano,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Veiculo> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByPlaca(placa: PlacaVeiculo): Optional<Veiculo> =
		Optional.ofNullable(jpa.findByPlaca(placa.normalizada)).map { it.toDomain() }

	override fun findByClienteId(clienteId: UUID): List<Veiculo> =
		jpa.findByCliente_Id(clienteId.toString()).map { it.toDomain() }

	override fun findAll(): List<Veiculo> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
