package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.OrdemServicoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/technician/ordens-servico")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_TECHNICIAN')")
class TechnicianOrdemServicoController(
	private val workOrders: OrdemServicoApplicationService,
) {

	@GetMapping
	fun list(): List<OrdemServicoResponse> = workOrders.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): OrdemServicoResponse = workOrders.get(id)

	@PostMapping("/{id}/iniciar-diagnostico")
	fun startDiagnosis(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.startDiagnosis(id)

	@PostMapping("/{id}/submeter-plano")
	fun submitPlan(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.submitPlanForInternalApproval(id)

	@PostMapping("/{id}/concluir-servicos")
	fun completeServices(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.completeServices(id)
}
