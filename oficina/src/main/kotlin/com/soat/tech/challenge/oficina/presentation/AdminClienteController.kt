package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.ClienteApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.ClienteRequest
import com.soat.tech.challenge.oficina.application.api.dto.ClienteResponse
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
@RequestMapping("/api/admin/clientes")
@SecurityRequirement(name = "bearer-jwt")
class AdminClienteController(
	private val customers: ClienteApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody req: ClienteRequest): ClienteResponse = customers.create(req)

	@GetMapping
	fun list(): List<ClienteResponse> = customers.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): ClienteResponse = customers.get(id)

	@PutMapping("/{id}")
	fun update(@PathVariable id: UUID, @Valid @RequestBody req: ClienteRequest): ClienteResponse =
		customers.update(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) {
		customers.delete(id)
	}
}
