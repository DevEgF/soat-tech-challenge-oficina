package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.TempoMedioServicoResponse
import com.soat.tech.challenge.oficina.domain.port.MetricasServicoPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MetricasApplicationService(
	private val metrics: MetricasServicoPort,
) {

	@Transactional(readOnly = true)
	fun averageTimeByService(): List<TempoMedioServicoResponse> =
		metrics.averageExecutionTimeByService().map {
			TempoMedioServicoResponse(
				catalogServiceId = it.catalogServiceId,
				serviceName = it.serviceName,
				averageMinutes = it.averageMinutes,
				sampleCount = it.sampleCount,
			)
		}
}
