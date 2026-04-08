package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.VehicleApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.VehicleRequest
import com.soat.tech.challenge.oficina.application.api.dto.VehicleResponse
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
@RequestMapping("/api/admin/veiculos")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
class AdminVehicleController(
	private val vehicles: VehicleApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody req: VehicleRequest): VehicleResponse = vehicles.create(req)

	@GetMapping
	fun list(): List<VehicleResponse> = vehicles.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): VehicleResponse = vehicles.get(id)

	@PutMapping("/{id}")
	fun update(@PathVariable id: UUID, @Valid @RequestBody req: VehicleRequest): VehicleResponse =
		vehicles.update(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) {
		vehicles.delete(id)
	}
}
