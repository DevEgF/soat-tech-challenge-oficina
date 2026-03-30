package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ClienteRequest
import com.soat.tech.challenge.oficina.application.api.dto.ClienteResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ClienteApplicationService(
	private val customers: ClienteRepository,
) {

	@Transactional
	fun create(req: ClienteRequest): ClienteResponse {
		val doc = DocumentoFiscal.parse(req.taxIdDigits)
		customers.findByFiscalDocument(doc).ifPresent { throw IllegalArgumentException("Customer already exists for this document") }
		val c = Cliente(
			id = UUID.randomUUID(),
			fiscalDocument = doc,
			name = req.name.trim(),
			email = req.email?.trim()?.takeIf { it.isNotEmpty() },
			phone = req.phone?.trim()?.takeIf { it.isNotEmpty() },
		)
		return customers.save(c).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<ClienteResponse> = customers.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): ClienteResponse =
		customers.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Customer not found") }

	@Transactional
	fun update(id: UUID, req: ClienteRequest): ClienteResponse {
		val existing = customers.findById(id).orElseThrow { NotFoundException("Customer not found") }
		val doc = DocumentoFiscal.parse(req.taxIdDigits)
		customers.findByFiscalDocument(doc).ifPresent { other ->
			if (other.id != id) throw IllegalArgumentException("Document already used by another customer")
		}
		val updated = existing.copy(
			fiscalDocument = doc,
			name = req.name.trim(),
			email = req.email?.trim()?.takeIf { it.isNotEmpty() },
			phone = req.phone?.trim()?.takeIf { it.isNotEmpty() },
		)
		return customers.save(updated).toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		if (customers.findById(id).isEmpty) throw NotFoundException("Customer not found")
		customers.deleteById(id)
	}
}

class NotFoundException(message: String) : RuntimeException(message)
