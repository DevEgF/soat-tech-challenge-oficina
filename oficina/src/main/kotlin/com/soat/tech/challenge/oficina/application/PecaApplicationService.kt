package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.PecaRequest
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.application.api.dto.PecaResponse
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PecaApplicationService(
	private val repo: PecaRepository,
) {

	@Transactional
	fun criar(req: PecaRequest): PecaResponse {
		val codigo = req.codigo.trim()
		repo.findByCodigo(codigo).ifPresent { throw IllegalArgumentException("Código de peça já existe") }
		val p = Peca(
			id = UUID.randomUUID(),
			codigo = codigo,
			nome = req.nome.trim(),
			precoCentavos = req.precoCentavos!!,
			quantidadeEstoque = req.quantidadeEstoque!!,
		)
		return repo.save(p).toResponse()
	}

	@Transactional(readOnly = true)
	fun listar(): List<PecaResponse> = repo.findAll().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun obter(id: UUID): PecaResponse =
		repo.findById(id).map { it.toResponse() }.orElseThrow { NotFoundException("Peça não encontrada") }

	@Transactional
	fun atualizar(id: UUID, req: PecaRequest): PecaResponse {
		val e = repo.findById(id).orElseThrow { NotFoundException("Peça não encontrada") }
		val codigo = req.codigo.trim()
		repo.findByCodigo(codigo).ifPresent { outro ->
			if (outro.id != id) throw IllegalArgumentException("Código já usado por outra peça")
		}
		val atualizado = e.copy(
			codigo = codigo,
			nome = req.nome.trim(),
			precoCentavos = req.precoCentavos!!,
			quantidadeEstoque = req.quantidadeEstoque!!,
		)
		return repo.save(atualizado).toResponse()
	}

	@Transactional
	fun excluir(id: UUID) {
		if (repo.findById(id).isEmpty) throw NotFoundException("Peça não encontrada")
		repo.deleteById(id)
	}
}
