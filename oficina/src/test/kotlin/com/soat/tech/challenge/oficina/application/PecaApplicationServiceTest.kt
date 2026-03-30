package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.EntradaMercadoriaRequest
import com.soat.tech.challenge.oficina.application.api.dto.PecaRequest
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import io.mockk.every
import io.mockk.mockk
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PecaApplicationServiceTest {

	private val repo = mockk<PecaRepository>()
	private val service = PecaApplicationService(repo)

	@Nested
	@DisplayName("Given new part code")
	inner class GivenNewCode {

		@Test
		@DisplayName("when create then persists")
		fun create() {
			every { repo.findByCode("P1") } returns Optional.empty()
			every { repo.save(any()) } answers { firstArg() }
			val r = service.create(PecaRequest(code = "P1", name = "Filtro", priceCents = 10, stockQuantity = 5))
			assertEquals("P1", r.code)
		}
	}

	@Nested
	@DisplayName("Given code already exists")
	inner class GivenDuplicateCode {

		@Test
		@DisplayName("when create then throws")
		fun duplicate() {
			every { repo.findByCode("P1") } returns Optional.of(
				Peca(UUID.randomUUID(), "P1", "X", 1, 1),
			)
			assertFailsWith<IllegalArgumentException> {
				service.create(PecaRequest(code = "P1", name = "Filtro", priceCents = 10, stockQuantity = 5))
			}
		}
	}

	@Nested
	@DisplayName("Given part to update")
	inner class GivenUpdate {

		@Test
		@DisplayName("when update with new code then returns new price")
		fun update() {
			val id = UUID.randomUUID()
			val p = Peca(id, "P1", "N", 5, 1)
			every { repo.findById(id) } returns Optional.of(p)
			every { repo.findByCode("P2") } returns Optional.of(p)
			every { repo.save(any()) } answers { firstArg() }
			val out = service.update(id, PecaRequest(code = "P2", name = "N2", priceCents = 6, stockQuantity = 2))
			assertEquals(6, out.priceCents)
		}
	}

	@Nested
	@DisplayName("recordGoodsReceipt")
	inner class GoodsReceipt {

		@Test
		@DisplayName("when part exists then increases stock")
		fun receipt() {
			val id = UUID.randomUUID()
			val p = Peca(id, "P1", "N", 5, 10)
			every { repo.findById(id) } returns Optional.of(p)
			every { repo.save(any()) } answers { firstArg() }
			val out = service.recordGoodsReceipt(id, EntradaMercadoriaRequest(quantity = 3, reference = "NF-1"))
			assertEquals(13, out.stockQuantity)
		}

		@Test
		@DisplayName("when part missing then NotFoundException")
		fun missing() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> {
				service.recordGoodsReceipt(id, EntradaMercadoriaRequest(quantity = 1))
			}
		}
	}

	@Nested
	@DisplayName("list and get")
	inner class ListGet {

		@Test
		fun list() {
			val id = UUID.randomUUID()
			every { repo.findAll() } returns listOf(Peca(id, "A", "B", 1, 0))
			assertEquals(1, service.list().size)
		}

		@Test
		fun get() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.of(Peca(id, "A", "B", 1, 0))
			assertEquals("A", service.get(id).code)
		}

		@Test
		fun getMissing() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> { service.get(id) }
		}
	}

	@Nested
	@DisplayName("delete")
	inner class DeletePart {

		@Test
		fun ok() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.of(Peca(id, "A", "B", 1, 0))
			every { repo.deleteById(id) } returns Unit
			service.delete(id)
		}

		@Test
		fun missing() {
			val id = UUID.randomUUID()
			every { repo.findById(id) } returns Optional.empty()
			assertFailsWith<NotFoundException> { service.delete(id) }
		}
	}
}
