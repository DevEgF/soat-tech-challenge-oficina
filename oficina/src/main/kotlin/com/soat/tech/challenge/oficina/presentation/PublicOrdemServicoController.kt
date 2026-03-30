package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.OrdemServicoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.AcompanhamentoOsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/os")
class PublicOrdemServicoController(
	private val workOrders: OrdemServicoApplicationService,
) {

	@GetMapping("/acompanhar")
	fun track(
		@RequestParam documento: String,
		@RequestParam codigo: String,
	): AcompanhamentoOsResponse = workOrders.track(documento, codigo)

	@PostMapping("/aprovar-orcamento")
	fun approveQuote(
		@RequestParam documento: String,
		@RequestParam codigo: String,
	): AcompanhamentoOsResponse = workOrders.approveCustomerQuote(documento, codigo)

	@PostMapping("/reprovar-orcamento")
	fun rejectQuote(
		@RequestParam documento: String,
		@RequestParam codigo: String,
	): AcompanhamentoOsResponse = workOrders.rejectCustomerQuote(documento, codigo)
}
