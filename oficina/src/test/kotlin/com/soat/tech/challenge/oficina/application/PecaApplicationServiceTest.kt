package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.PecaRequest
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PecaApplicationServiceTest {

	private val repo = mockk<PecaRepository>()
	private val service = PecaApplicationService(repo)

	@Test
	fun `criar peca`() {
		every { repo.findByCodigo("P1") } returns Optional.empty()
		every { repo.save(any()) } answers { firstArg() }
		val r = service.criar(PecaRequest(codigo = "P1", nome = "Filtro", precoCentavos = 10, quantidadeEstoque = 5))
		assertEquals("P1", r.codigo)
	}

	@Test
	fun `criar falha codigo duplicado`() {
		every { repo.findByCodigo("P1") } returns Optional.of(
			Peca(UUID.randomUUID(), "P1", "X", 1, 1),
		)
		assertFailsWith<IllegalArgumentException> {
			service.criar(PecaRequest(codigo = "P1", nome = "Filtro", precoCentavos = 10, quantidadeEstoque = 5))
		}
	}

	@Test
	fun `atualizar e obter`() {
		val id = UUID.randomUUID()
		val p = Peca(id, "P2", "N", 5, 1)
		every { repo.findById(id) } returns Optional.of(p)
		every { repo.findByCodigo("P2") } returns Optional.of(p)
		every { repo.save(any()) } answers { firstArg() }
		val out = service.atualizar(id, PecaRequest(codigo = "P2", nome = "N2", precoCentavos = 6, quantidadeEstoque = 2))
		assertEquals(6, out.precoCentavos)
		assertEquals("N2", out.nome)
	}
}
