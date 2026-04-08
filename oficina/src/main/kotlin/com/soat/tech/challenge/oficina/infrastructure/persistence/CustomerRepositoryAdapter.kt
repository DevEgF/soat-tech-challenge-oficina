package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Customer
import com.soat.tech.challenge.oficina.domain.model.TaxDocument
import com.soat.tech.challenge.oficina.domain.port.CustomerRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.CustomerJpaRepository
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class CustomerRepositoryAdapter(
	private val jpa: CustomerJpaRepository,
) : CustomerRepository {

	override fun save(cliente: Customer): Customer {
		val e = cliente.toEntity()
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Customer> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByFiscalDocument(document: TaxDocument): Optional<Customer> =
		Optional.ofNullable(jpa.findByDocumentDigits(document.digits)).map { it.toDomain() }

	override fun findAll(): List<Customer> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
