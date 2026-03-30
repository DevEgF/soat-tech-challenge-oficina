package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.OrdemServicoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.AcompanhamentoOsResponse
import org.springframework.web.bind.annotation.GetMapping
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
}
