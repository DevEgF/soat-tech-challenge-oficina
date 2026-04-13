package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.AverageServiceTimeResponse
import com.soat.tech.challenge.oficina.domain.port.MetricsServicePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MetricsApplicationService(
	private val metrics: MetricsServicePort,
) {

	@Transactional(readOnly = true)
	fun averageTimeByService(): List<AverageServiceTimeResponse> =
		metrics.averageExecutionTimeByService().map {
			AverageServiceTimeResponse(
				catalogServiceId = it.catalogServiceId,
				serviceName = it.serviceName,
				averageMinutes = it.averageMinutes,
				sampleCount = it.sampleCount,
			)
		}
}
