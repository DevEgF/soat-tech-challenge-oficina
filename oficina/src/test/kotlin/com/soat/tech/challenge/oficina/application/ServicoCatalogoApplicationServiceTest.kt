package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import io.mockk.every
import io.mockk.mockk
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ServicoCatalogoApplicationServiceTest {

	private val repo = mockk<ServicoCatalogoRepository>()
	private val service = ServicoCatalogoApplicationService(repo)

	@Nested
	@DisplayName("Given save succeeds")
	inner class GivenSave {

		@Test
		@DisplayName("when create and get then name matches")
		fun createAndGet() {
			every { repo.save(any()) } answers { firstArg() }
			val c = service.create(ServicoCatalogoRequest(name = "S", priceCents = 100, estimatedMinutes = 30))
			val id = c.id
			every { repo.findById(id) } returns Optional.of(
				ServicoCatalogo(id, "S", null, 100, 30),
			)
			assertEquals("S", service.get(id).name)
		}
	}

	@Test
	@DisplayName("when get missing id then NotFoundException")
	fun getMissing() {
		val id = UUID.randomUUID()
		every { repo.findById(id) } returns Optional.empty()
		assertFailsWith<NotFoundException> { service.get(id) }
	}

	@Test
	@DisplayName("when list empty repo then empty list")
	fun list() {
		every { repo.findAll() } returns emptyList()
		assertEquals(0, service.list().size)
	}

	@Test
	@DisplayName("when update and delete then repository invoked")
	fun updateAndDelete() {
		val id = UUID.randomUUID()
		val e = ServicoCatalogo(id, "A", null, 1, 1)
		every { repo.findById(id) } returns Optional.of(e)
		every { repo.save(any()) } answers { firstArg() }
		service.update(id, ServicoCatalogoRequest(name = "B", priceCents = 2, estimatedMinutes = 2))
		every { repo.findById(id) } returns Optional.of(e)
		every { repo.deleteById(id) } returns Unit
		service.delete(id)
	}
}
