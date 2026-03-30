package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.OrdemServicoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/attendant/ordens-servico")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_ATTENDANT')")
class AttendantOrdemServicoController(
	private val workOrders: OrdemServicoApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody req: CriarOrdemServicoRequest): OrdemServicoResponse =
		workOrders.create(req)

	@GetMapping
	fun list(): List<OrdemServicoResponse> = workOrders.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): OrdemServicoResponse = workOrders.get(id)

	@PostMapping("/{id}/enviar-orcamento-cliente")
	fun sendQuoteToCustomer(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.sendQuoteToCustomer(id)

	@PostMapping("/{id}/voltar-diagnostico")
	fun returnToDiagnosis(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.returnToDiagnosis(id)

	@PostMapping("/{id}/registrar-entrega")
	fun registerDelivery(@PathVariable id: UUID): OrdemServicoResponse =
		workOrders.registerDelivery(id)
}
