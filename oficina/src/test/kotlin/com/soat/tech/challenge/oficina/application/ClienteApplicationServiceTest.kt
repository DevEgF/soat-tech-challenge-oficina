package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ClienteRequest
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ClienteApplicationServiceTest {

	private val repo = mockk<ClienteRepository>()
	private val service = ClienteApplicationService(repo)

	@Nested
	@DisplayName("Given unique tax document")
	inner class GivenUniqueDocument {

		@Test
		@DisplayName("when create then saves and returns name")
		fun createPersists() {
			val doc = DocumentoFiscal.parse("52998224725")
			every { repo.findByFiscalDocument(doc) } returns Optional.empty()
			every { repo.save(any()) } answers { firstArg() }
			val r = service.create(ClienteRequest(taxIdDigits = "52998224725", name = "Ana"))
			assertEquals("Ana", r.name)
			verify(exactly = 1) { repo.save(any()) }
		}
	}

	@Nested
	@DisplayName("Given document already registered")
	inner class GivenDuplicateDocument {

		@Test
		@DisplayName("when create then throws")
		fun createFails() {
			val doc = DocumentoFiscal.parse("52998224725")
			val existing = Cliente(UUID.randomUUID(), doc, "X")
			every { repo.findByFiscalDocument(doc) } returns Optional.of(existing)
			assertFailsWith<IllegalArgumentException> {
				service.create(ClienteRequest(taxIdDigits = "52998224725", name = "Ana"))
			}
		}
	}

	@Nested
	@DisplayName("Given existing customer id")
	inner class GivenExistingId {

		@Test
		@DisplayName("when get then returns customer")
		fun getReturns() {
			val id = UUID.randomUUID()
			val c = Cliente(id, DocumentoFiscal.parse("52998224725"), "Ana")
			every { repo.findById(id) } returns Optional.of(c)
			assertEquals("Ana", service.get(id).name)
		}

		@Test
		@DisplayName("when get missing id then NotFoundException")
		fun getMissing() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> { service.get(id) }
		}
	}

	@Test
	@DisplayName("Given empty repo when list then empty")
	fun listDelegates() {
		every { repo.findAll() } returns emptyList()
		assertEquals(0, service.list().size)
	}

	@Nested
	@DisplayName("Given customer to update")
	inner class GivenUpdate {

		@Test
		@DisplayName("when update then new name persisted")
		fun update() {
			val id = UUID.randomUUID()
			val doc = DocumentoFiscal.parse("52998224725")
			val existing = Cliente(id, doc, "Old")
			every { repo.findById(id) } returns Optional.of(existing)
			every { repo.findByFiscalDocument(doc) } returns Optional.of(existing)
			every { repo.save(any()) } answers { firstArg() }
			val r = service.update(id, ClienteRequest(taxIdDigits = "52998224725", name = "New"))
			assertEquals("New", r.name)
		}
	}

	@Test
	@DisplayName("Given customer when delete then repository delete")
	fun delete() {
		val id = UUID.randomUUID()
		every { repo.findById(id) } returns Optional.of(
			Cliente(id, DocumentoFiscal.parse("52998224725"), "A"),
		)
		every { repo.deleteById(id) } returns Unit
		service.delete(id)
		verify { repo.deleteById(id) }
	}
}
