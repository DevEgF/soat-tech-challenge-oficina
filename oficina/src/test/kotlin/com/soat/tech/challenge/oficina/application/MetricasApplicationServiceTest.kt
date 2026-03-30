package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.domain.port.MetricasServicoPort
import com.soat.tech.challenge.oficina.domain.port.TempoMedioServicoDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class MetricasApplicationServiceTest {

	private val port = mockk<MetricasServicoPort>()
	private val service = MetricasApplicationService(port)

	@Test
	fun `delega port`() {
		val id = UUID.randomUUID()
		every { port.tempoMedioExecucaoPorServico() } returns listOf(
			TempoMedioServicoDto(id, "S", 10.0, 2),
		)
		val r = service.tempoMedioPorServico()
		assertEquals(1, r.size)
		assertEquals(10.0, r[0].mediaMinutos)
	}
}
