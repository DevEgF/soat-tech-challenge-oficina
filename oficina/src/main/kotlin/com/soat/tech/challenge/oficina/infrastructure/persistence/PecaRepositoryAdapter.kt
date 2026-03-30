package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PecaJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PecaEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class PecaRepositoryAdapter(
	private val jpa: PecaJpaRepository,
) : PecaRepository {

	override fun save(peca: Peca): Peca {
		val e = PecaEntity(
			id = peca.id.toString(),
			codigo = peca.codigo,
			nome = peca.nome,
			precoCentavos = peca.precoCentavos,
			quantidadeEstoque = peca.quantidadeEstoque,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<Peca> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findByCodigo(codigo: String): Optional<Peca> =
		Optional.ofNullable(jpa.findByCodigo(codigo)).map { it.toDomain() }

	override fun findAll(): List<Peca> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
