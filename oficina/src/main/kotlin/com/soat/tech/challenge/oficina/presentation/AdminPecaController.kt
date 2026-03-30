package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.PecaApplicationService
import com.soat.tech.challenge.oficina.application.api.dto.EntradaMercadoriaRequest
import com.soat.tech.challenge.oficina.application.api.dto.PecaRequest
import com.soat.tech.challenge.oficina.application.api.dto.PecaResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
@RequestMapping("/api/admin/pecas")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
class AdminPecaController(
	private val parts: PecaApplicationService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody req: PecaRequest): PecaResponse = parts.create(req)

	@GetMapping
	fun list(): List<PecaResponse> = parts.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): PecaResponse = parts.get(id)

	@PutMapping("/{id}")
	fun update(@PathVariable id: UUID, @Valid @RequestBody req: PecaRequest): PecaResponse =
		parts.update(id, req)

	@PostMapping("/{id}/entrada-mercadoria")
	fun goodsReceipt(
		@PathVariable id: UUID,
		@Valid @RequestBody req: EntradaMercadoriaRequest,
	): PecaResponse = parts.recordGoodsReceipt(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) {
		parts.delete(id)
	}
}
