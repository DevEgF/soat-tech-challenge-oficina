package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.ServicoCatalogoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/servicos-catalogo")
@SecurityRequirement(name = "bearer-jwt")
class AdminServicoCatalogoController(
	private val servicos: ServicoCatalogoApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun criar(@Valid @RequestBody req: ServicoCatalogoRequest): ServicoCatalogoResponse = servicos.criar(req)

	@GetMapping
	fun listar(): List<ServicoCatalogoResponse> = servicos.listar()

	@GetMapping("/{id}")
	fun obter(@PathVariable id: UUID): ServicoCatalogoResponse = servicos.obter(id)

	@PutMapping("/{id}")
	fun atualizar(
		@PathVariable id: UUID,
		@Valid @RequestBody req: ServicoCatalogoRequest,
	): ServicoCatalogoResponse = servicos.atualizar(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun excluir(@PathVariable id: UUID) {
		servicos.excluir(id)
	}
}
