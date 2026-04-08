package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.domain.port.AverageServiceTimeDto
import com.soat.tech.challenge.oficina.domain.port.MetricsServicePort
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MetricsApplicationServiceTest {

	private val port = mockk<MetricsServicePort>()
	private val service = MetricsApplicationService(port)

	@Nested
	@DisplayName("Given port returns samples")
	inner class GivenSamples {

		@Test
		@DisplayName("when averageTimeByService then maps DTO fields")
		fun mapsFields() {
			val sid = UUID.randomUUID()
			every { port.averageExecutionTimeByService() } returns listOf(
				AverageServiceTimeDto(sid, "S", 10.0, 2),
			)
			val r = service.averageTimeByService()
			assertEquals(10.0, r[0].averageMinutes)
			assertEquals("S", r[0].serviceName)
		}
	}
}
