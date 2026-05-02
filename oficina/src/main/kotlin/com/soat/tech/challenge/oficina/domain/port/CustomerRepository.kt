package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.Customer
import com.soat.tech.challenge.oficina.domain.model.TaxDocument
import java.util.Optional
import java.util.UUID

interface CustomerRepository {
	fun save(cliente: Customer): Customer
	fun findById(id: UUID): Optional<Customer>
	fun findByFiscalDocument(document: TaxDocument): Optional<Customer>
	fun findAll(): List<Customer>
	fun deleteById(id: UUID)
}
