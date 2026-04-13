package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.CustomerRequest
import com.soat.tech.challenge.oficina.application.api.dto.CustomerResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.BusinessRuleException
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import com.soat.tech.challenge.oficina.domain.model.Customer
import com.soat.tech.challenge.oficina.domain.model.TaxDocument
import com.soat.tech.challenge.oficina.domain.port.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CustomerApplicationService(
	private val customers: CustomerRepository,
) {

	@Transactional
	fun create(req: CustomerRequest): CustomerResponse {
		val doc = TaxDocument.parse(req.taxIdDigits)
		customers.findByFiscalDocument(doc).ifPresent { throw BusinessRuleException("Customer already exists for this document") }
		val c = Customer(
			id = UUID.randomUUID(),
			fiscalDocument = doc,
			name = req.name.trim(),
			email = req.email?.trim()?.takeIf { it.isNotEmpty() },
			phone = req.phone?.trim()?.takeIf { it.isNotEmpty() },
		)
		return customers.save(c).toResponse()
	}

	@Transactional(readOnly = true)
	fun list(): List<CustomerResponse> = customers.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun get(id: UUID): CustomerResponse =
		customers.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Customer not found") }

	@Transactional
	fun update(id: UUID, req: CustomerRequest): CustomerResponse {
		val existing = customers.findById(id).orElseThrow { NotFoundException("Customer not found") }
		val doc = TaxDocument.parse(req.taxIdDigits)
		customers.findByFiscalDocument(doc).ifPresent { other ->
			if (other.id != id) throw BusinessRuleException("Document already used by another customer")
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
