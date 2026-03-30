package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoRequest
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ServicoCatalogoApplicationService(
	private val repo: ServicoCatalogoRepository,
) {

	@Transactional
	fun criar(req: ServicoCatalogoRequest): ServicoCatalogoResponse {
		val s = ServicoCatalogo(
			id = UUID.randomUUID(),
			nome = req.nome.trim(),
			descricao = req.descricao?.trim()?.takeIf { it.isNotEmpty() },
			precoCentavos = req.precoCentavos!!,
			tempoEstimadoMinutos = req.tempoEstimadoMinutos!!,
		)
		return repo.save(s).toResponse()
	}

	@Transactional(readOnly = true)
	fun listar(): List<ServicoCatalogoResponse> = repo.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun obter(id: UUID): ServicoCatalogoResponse =
		repo.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Serviço não encontrado") }

	@Transactional
	fun atualizar(id: UUID, req: ServicoCatalogoRequest): ServicoCatalogoResponse {
		val e = repo.findById(id).orElseThrow { NotFoundException("Serviço não encontrado") }
		val atualizado = e.copy(
			nome = req.nome.trim(),
			descricao = req.descricao?.trim()?.takeIf { it.isNotEmpty() },
			precoCentavos = req.precoCentavos!!,
			tempoEstimadoMinutos = req.tempoEstimadoMinutos!!,
		)
		return repo.save(atualizado).toResponse()
	}

	@Transactional
	fun excluir(id: UUID) {
		if (repo.findById(id).isEmpty) throw NotFoundException("Serviço não encontrado")
		repo.deleteById(id)
	}
}
