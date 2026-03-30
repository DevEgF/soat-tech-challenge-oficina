package com.soat.tech.challenge.oficina.application

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
}
