package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.VeiculoRequest
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoResponse
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VeiculoApplicationService(
	private val veiculos: VeiculoRepository,
	private val clientes: ClienteRepository,
) {

	@Transactional
	fun criar(req: VeiculoRequest): VeiculoResponse {
		val clienteId = req.clienteId!!
		clientes.findById(clienteId).orElseThrow { NotFoundException("Cliente não encontrado") }
		val placa = PlacaVeiculo.parse(req.placa)
		veiculos.findByPlaca(placa).ifPresent { throw IllegalArgumentException("Placa já cadastrada") }
		val v = Veiculo(
			id = UUID.randomUUID(),
			clienteId = clienteId,
			placa = placa,
			marca = req.marca.trim(),
			modelo = req.modelo.trim(),
			ano = req.ano!!,
		)
		return veiculos.save(v).toResponse()
	}

	@Transactional(readOnly = true)
	fun listar(): List<VeiculoResponse> = veiculos.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun obter(id: UUID): VeiculoResponse =
		veiculos.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Veículo não encontrado") }

	@Transactional
	fun atualizar(id: UUID, req: VeiculoRequest): VeiculoResponse {
		val existente = veiculos.findById(id).orElseThrow { NotFoundException("Veículo não encontrado") }
		val clienteId = req.clienteId!!
		clientes.findById(clienteId).orElseThrow { NotFoundException("Cliente não encontrado") }
		val placa = PlacaVeiculo.parse(req.placa)
		veiculos.findByPlaca(placa).ifPresent { outro ->
			if (outro.id != id) throw IllegalArgumentException("Placa já usada por outro veículo")
		}
		val atualizado = existente.copy(
			clienteId = clienteId,
			placa = placa,
			marca = req.marca.trim(),
			modelo = req.modelo.trim(),
			ano = req.ano!!,
		)
		return veiculos.save(atualizado).toResponse()
	}

	@Transactional
	fun excluir(id: UUID) {
		if (veiculos.findById(id).isEmpty) throw NotFoundException("Veículo não encontrado")
		veiculos.deleteById(id)
	}
}
