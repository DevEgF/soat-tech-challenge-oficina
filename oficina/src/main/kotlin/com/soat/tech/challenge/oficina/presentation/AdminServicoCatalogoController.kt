package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.ServicoCatalogoApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
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
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
class AdminServicoCatalogoController(
	private val catalogServices: ServicoCatalogoApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody req: ServicoCatalogoRequest): ServicoCatalogoResponse =
		catalogServices.create(req)

	@GetMapping
	fun list(): List<ServicoCatalogoResponse> = catalogServices.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): ServicoCatalogoResponse = catalogServices.get(id)

	@PutMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody req: ServicoCatalogoRequest,
	): ServicoCatalogoResponse = catalogServices.update(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) {
		catalogServices.delete(id)
	}
}
