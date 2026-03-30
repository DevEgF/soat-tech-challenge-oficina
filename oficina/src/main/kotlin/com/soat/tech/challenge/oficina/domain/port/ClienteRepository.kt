package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import java.util.Optional
import java.util.UUID

interface ClienteRepository {
	fun save(cliente: Cliente): Cliente
	fun findById(id: UUID): Optional<Cliente>
	fun findByDocumento(documento: DocumentoFiscal): Optional<Cliente>
	fun findAll(): List<Cliente>
	fun deleteById(id: UUID)
}
