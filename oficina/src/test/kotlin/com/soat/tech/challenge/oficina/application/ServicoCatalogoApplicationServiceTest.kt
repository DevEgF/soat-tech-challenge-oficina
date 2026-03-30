package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServicoCatalogoApplicationServiceTest {

	private val repo = mockk<ServicoCatalogoRepository>()
	private val service = ServicoCatalogoApplicationService(repo)

	@Test
	fun `criar e obter`() {
		every { repo.save(any()) } answers { firstArg() }
		val c = service.criar(ServicoCatalogoRequest(nome = "S", precoCentavos = 100, tempoEstimadoMinutos = 30))
		val id = c.id
		every { repo.findById(id) } returns Optional.of(
			ServicoCatalogo(id, "S", null, 100, 30),
		)
		assertEquals("S", service.obter(id).nome)
	}

	@Test
	fun `obter inexistente`() {
		val id = UUID.randomUUID()
		every { repo.findById(id) } returns Optional.empty()
		assertFailsWith<NotFoundException> { service.obter(id) }
	}

	@Test
	fun `listar`() {
		every { repo.findAll() } returns emptyList()
		assertEquals(0, service.listar().size)
	}

	@Test
	fun `atualizar e excluir`() {
		val id = UUID.randomUUID()
		val e = ServicoCatalogo(id, "A", null, 1, 1)
		every { repo.findById(id) } returns Optional.of(e)
		every { repo.save(any()) } answers { firstArg() }
		service.atualizar(id, ServicoCatalogoRequest(nome = "B", precoCentavos = 2, tempoEstimadoMinutos = 2))
		every { repo.findById(id) } returns Optional.of(e)
		every { repo.deleteById(id) } returns Unit
		service.excluir(id)
	}
}
