package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ClienteRequest
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClienteApplicationServiceTest {

	private val repo = mockk<ClienteRepository>()
	private val service = ClienteApplicationService(repo)

	@Test
	fun `criar persiste cliente`() {
		val doc = DocumentoFiscal.parse("52998224725")
		every { repo.findByDocumento(doc) } returns Optional.empty()
		every { repo.save(any()) } answers { firstArg() }
		val r = service.criar(ClienteRequest(documento = "52998224725", nome = "Ana"))
		assertEquals("Ana", r.nome)
		verify(exactly = 1) { repo.save(any()) }
	}

	@Test
	fun `criar falha se documento existe`() {
		val doc = DocumentoFiscal.parse("52998224725")
		val existente = Cliente(UUID.randomUUID(), doc, "X")
		every { repo.findByDocumento(doc) } returns Optional.of(existente)
		assertFailsWith<IllegalArgumentException> {
			service.criar(ClienteRequest(documento = "52998224725", nome = "Ana"))
		}
	}

	@Test
	fun `obter retorna cliente`() {
		val id = UUID.randomUUID()
		val c = Cliente(id, DocumentoFiscal.parse("52998224725"), "Ana")
		every { repo.findById(id) } returns Optional.of(c)
		assertEquals("Ana", service.obter(id).nome)
	}

	@Test
	fun `obter lanca se nao existe`() {
		val id = UUID.randomUUID()
		every { repo.findById(id) } returns Optional.empty()
		assertFailsWith<NotFoundException> { service.obter(id) }
	}

	@Test
	fun `listar delega repositorio`() {
		every { repo.findAll() } returns emptyList()
		assertEquals(0, service.listar().size)
	}

	@Test
	fun `atualiza cliente`() {
		val id = UUID.randomUUID()
		val doc = DocumentoFiscal.parse("52998224725")
		val existente = Cliente(id, doc, "Old")
		every { repo.findById(id) } returns Optional.of(existente)
		every { repo.findByDocumento(doc) } returns Optional.of(existente)
		every { repo.save(any()) } answers { firstArg() }
		val r = service.atualizar(id, ClienteRequest(documento = "52998224725", nome = "New"))
		assertEquals("New", r.nome)
	}

	@Test
	fun `excluir remove`() {
		val id = UUID.randomUUID()
		every { repo.findById(id) } returns Optional.of(
			Cliente(id, DocumentoFiscal.parse("52998224725"), "A"),
		)
		every { repo.deleteById(id) } returns Unit
		service.excluir(id)
		verify { repo.deleteById(id) }
	}
}
