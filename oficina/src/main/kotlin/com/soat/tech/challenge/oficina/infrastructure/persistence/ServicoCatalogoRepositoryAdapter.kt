package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ServicoCatalogoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ServicoCatalogoEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class ServicoCatalogoRepositoryAdapter(
	private val jpa: ServicoCatalogoJpaRepository,
) : ServicoCatalogoRepository {

	override fun save(servico: ServicoCatalogo): ServicoCatalogo {
		val e = ServicoCatalogoEntity(
			id = servico.id.toString(),
			nome = servico.nome,
			descricao = servico.descricao,
			precoCentavos = servico.precoCentavos,
			tempoEstimadoMinutos = servico.tempoEstimadoMinutos,
		)
		return jpa.save(e).toDomain()
	}

	override fun findById(id: UUID): Optional<ServicoCatalogo> =
		jpa.findById(id.toString()).map { it.toDomain() }

	override fun findAll(): List<ServicoCatalogo> = jpa.findAll().map { it.toDomain() }

	override fun deleteById(id: UUID) {
		jpa.deleteById(id.toString())
	}
}
