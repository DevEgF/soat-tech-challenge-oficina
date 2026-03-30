package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.MetricasApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.TempoMedioServicoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/metricas")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
class AdminMetricasController(
	private val metrics: MetricasApplicationService,
) {

	@GetMapping("/tempo-medio-execucao-servicos")
	fun averageExecutionTimeByService(): List<TempoMedioServicoResponse> =
		metrics.averageTimeByService()
}
