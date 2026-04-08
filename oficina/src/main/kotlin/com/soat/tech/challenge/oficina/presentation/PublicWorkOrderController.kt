package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.WorkOrderApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.WorkOrderTrackingResponse
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/public/os")
class PublicWorkOrderController(
	private val workOrders: WorkOrderApplicationService,
) {

	@GetMapping("/acompanhar")
	fun track(
		@RequestParam @NotBlank documento: String,
		@RequestParam @NotBlank codigo: String,
	): WorkOrderTrackingResponse = workOrders.track(documento, codigo)

	@PostMapping("/aprovar-orcamento")
	fun approveQuote(
		@RequestParam @NotBlank documento: String,
		@RequestParam @NotBlank codigo: String,
	): WorkOrderTrackingResponse = workOrders.approveCustomerQuote(documento, codigo)

	@PostMapping("/reprovar-orcamento")
	fun rejectQuote(
		@RequestParam @NotBlank documento: String,
		@RequestParam @NotBlank codigo: String,
	): WorkOrderTrackingResponse = workOrders.rejectCustomerQuote(documento, codigo)
}
