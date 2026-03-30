package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.OrdemServicoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/ordens-servico")
@SecurityRequirement(name = "bearer-jwt")
class AdminOrdemServicoController(
	private val ordens: OrdemServicoApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun criar(@Valid @RequestBody req: CriarOrdemServicoRequest): OrdemServicoResponse = ordens.criar(req)

	@GetMapping
	fun listar(): List<OrdemServicoResponse> = ordens.listar()

	@GetMapping("/{id}")
	fun obter(@PathVariable id: UUID): OrdemServicoResponse = ordens.obter(id)

	@PostMapping("/{id}/iniciar-diagnostico")
	fun iniciarDiagnostico(@PathVariable id: UUID): OrdemServicoResponse = ordens.iniciarDiagnostico(id)

	@PostMapping("/{id}/enviar-orcamento")
	fun enviarOrcamento(@PathVariable id: UUID): OrdemServicoResponse = ordens.enviarOrcamento(id)

	@PostMapping("/{id}/aprovar-orcamento")
	fun aprovarOrcamento(@PathVariable id: UUID): OrdemServicoResponse = ordens.aprovarOrcamento(id)

	@PostMapping("/{id}/voltar-diagnostico")
	fun voltarDiagnostico(@PathVariable id: UUID): OrdemServicoResponse = ordens.voltarParaDiagnostico(id)

	@PostMapping("/{id}/concluir-servicos")
	fun concluir(@PathVariable id: UUID): OrdemServicoResponse = ordens.concluirServicos(id)

	@PostMapping("/{id}/registrar-entrega")
	fun entregar(@PathVariable id: UUID): OrdemServicoResponse = ordens.registrarEntrega(id)
}
