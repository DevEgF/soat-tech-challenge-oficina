package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import java.util.Optional
import java.util.UUID

interface VeiculoRepository {
	fun save(veiculo: Veiculo): Veiculo
	fun findById(id: UUID): Optional<Veiculo>
	fun findByPlaca(placa: PlacaVeiculo): Optional<Veiculo>
	fun findByClienteId(clienteId: UUID): List<Veiculo>
	fun findAll(): List<Veiculo>
	fun deleteById(id: UUID)
}
