package com.soat.tech.challenge.oficina.domain.port

import java.util.UUID

data class AverageServiceTimeDto(
	val catalogServiceId: UUID,
	val serviceName: String,
	val averageMinutes: Double,
	val sampleCount: Long,
)

interface MetricasServicoPort {
	fun averageExecutionTimeByService(): List<AverageServiceTimeDto>
}
