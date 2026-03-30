package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ClienteRequest
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.application.api.dto.ClienteResponse
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ClienteApplicationService(
	private val clientes: ClienteRepository,
) {

	@Transactional
	fun criar(req: ClienteRequest): ClienteResponse {
		val doc = DocumentoFiscal.parse(req.documento)
		clientes.findByDocumento(doc).ifPresent { throw IllegalArgumentException("Cliente já existe com este documento") }
		val c = Cliente(
			id = UUID.randomUUID(),
			documento = doc,
			nome = req.nome.trim(),
			email = req.email?.trim()?.takeIf { it.isNotEmpty() },
			telefone = req.telefone?.trim()?.takeIf { it.isNotEmpty() },
		)
		return clientes.save(c).toResponse()
	}

	@Transactional(readOnly = true)
	fun listar(): List<ClienteResponse> = clientes.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun obter(id: UUID): ClienteResponse =
		clientes.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Cliente não encontrado") }

	@Transactional
	fun atualizar(id: UUID, req: ClienteRequest): ClienteResponse {
		val existente = clientes.findById(id).orElseThrow { NotFoundException("Cliente não encontrado") }
		val doc = DocumentoFiscal.parse(req.documento)
		clientes.findByDocumento(doc).ifPresent { outro ->
			if (outro.id != id) throw IllegalArgumentException("Documento já usado por outro cliente")
		}
		val atualizado = existente.copy(
			documento = doc,
			nome = req.nome.trim(),
			email = req.email?.trim()?.takeIf { it.isNotEmpty() },
			telefone = req.telefone?.trim()?.takeIf { it.isNotEmpty() },
		)
		return clientes.save(atualizado).toResponse()
	}

	@Transactional
	fun excluir(id: UUID) {
		if (clientes.findById(id).isEmpty) throw NotFoundException("Cliente não encontrado")
		clientes.deleteById(id)
	}
}

class NotFoundException(message: String) : RuntimeException(message)
