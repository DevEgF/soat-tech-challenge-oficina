package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.WarehouseApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.EstoqueAlertaResponse
import com.soat.tech.challenge.oficina.application.api.dto.ReservaPecaOsResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/warehouse")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_WAREHOUSE')")
class WarehouseController(
	private val warehouse: WarehouseApplicationService,
) {

	@GetMapping("/ordens-servico/{id}/reservas-pendentes")
	fun listPendingReservations(@PathVariable id: UUID): List<ReservaPecaOsResponse> =
		warehouse.listPendingReservationsForWorkOrder(id)

	@PostMapping("/ordens-servico/{id}/confirmar-saida")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun confirmStockExit(@PathVariable id: UUID) {
		warehouse.confirmStockExitForWorkOrder(id)
	}

	@GetMapping("/alertas-estoque-baixo")
	fun lowStockAlerts(): List<EstoqueAlertaResponse> = warehouse.listLowStockAlerts()
}
