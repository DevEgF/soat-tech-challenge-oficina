package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.CatalogServiceApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.CatalogServiceResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/technician/servicos-catalogo")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_TECHNICIAN')")
class TechnicianCatalogServiceController(
	private val catalogServices: CatalogServiceApplicationService,
) {

	@GetMapping
	fun list(): List<CatalogServiceResponse> = catalogServices.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): CatalogServiceResponse = catalogServices.get(id)
}
