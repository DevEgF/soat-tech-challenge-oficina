package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.VeiculoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoRequest
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoResponse
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
@RequestMapping("/api/admin/veiculos")
@SecurityRequirement(name = "bearer-jwt")
class AdminVeiculoController(
	private val veiculos: VeiculoApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun criar(@Valid @RequestBody req: VeiculoRequest): VeiculoResponse = veiculos.criar(req)

	@GetMapping
	fun listar(): List<VeiculoResponse> = veiculos.listar()

	@GetMapping("/{id}")
	fun obter(@PathVariable id: UUID): VeiculoResponse = veiculos.obter(id)

	@PutMapping("/{id}")
	fun atualizar(@PathVariable id: UUID, @Valid @RequestBody req: VeiculoRequest): VeiculoResponse =
		veiculos.atualizar(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun excluir(@PathVariable id: UUID) {
		veiculos.excluir(id)
	}
}
