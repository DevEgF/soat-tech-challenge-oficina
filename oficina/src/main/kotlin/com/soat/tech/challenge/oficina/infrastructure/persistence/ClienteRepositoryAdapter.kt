package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ClienteJpaRepository
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class ClienteRepositoryAdapter(
	private val jpa: ClienteJpaRepository,
) : ClienteRepository {

	override fun save(cliente: Cliente): Cliente {
		val e = cliente.toEntity()
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Cliente> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByFiscalDocument(document: DocumentoFiscal): Optional<Cliente> =
		Optional.ofNullable(jpa.findByDocumentDigits(document.digits)).map { it.toDomain() }

	override fun findAll(): List<Cliente> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
