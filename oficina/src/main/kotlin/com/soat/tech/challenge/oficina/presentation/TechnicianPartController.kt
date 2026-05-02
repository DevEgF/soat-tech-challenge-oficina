package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.PartApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.PartAvailabilityResponse
import com.soat.tech.challenge.oficina.application.api.dto.PartResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/technician/pecas")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_TECHNICIAN')")
class TechnicianPartController(
	private val parts: PartApplicationService,
) {

	@GetMapping
	fun list(): List<PartResponse> = parts.list()

	@GetMapping("/disponibilidade")
	fun availability(): List<PartAvailabilityResponse> = parts.listAvailability()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): PartResponse = parts.get(id)
}
