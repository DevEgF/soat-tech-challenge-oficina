package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface OrdemServicoJpaRepository : JpaRepository<OrdemServicoEntity, String> {
	@Query(
		"""
		SELECT DISTINCT o FROM OrdemServicoEntity o
		LEFT JOIN FETCH o.cliente
		LEFT JOIN FETCH o.veiculo
		LEFT JOIN FETCH o.linhasServico ls
		LEFT JOIN FETCH ls.servicoCatalogo
		LEFT JOIN FETCH o.linhasPeca lp
		LEFT JOIN FETCH lp.peca
		WHERE o.id = :id
		""",
	)
	fun findByIdComDetalhes(id: String): Optional<OrdemServicoEntity>

	@Query(
		"""
		SELECT DISTINCT o FROM OrdemServicoEntity o
		LEFT JOIN FETCH o.cliente
		LEFT JOIN FETCH o.veiculo
		LEFT JOIN FETCH o.linhasServico ls
		LEFT JOIN FETCH ls.servicoCatalogo
		LEFT JOIN FETCH o.linhasPeca lp
		LEFT JOIN FETCH lp.peca
		WHERE o.codigoAcompanhamento = :codigo
		""",
	)
	fun findByCodigoAcompanhamentoComDetalhes(codigo: String): Optional<OrdemServicoEntity>

	@Query(
		"""
		SELECT DISTINCT o FROM OrdemServicoEntity o
		LEFT JOIN FETCH o.cliente
		LEFT JOIN FETCH o.veiculo
		LEFT JOIN FETCH o.linhasServico ls
		LEFT JOIN FETCH ls.servicoCatalogo
		LEFT JOIN FETCH o.linhasPeca lp
		LEFT JOIN FETCH lp.peca
		""",
	)
	fun findAllComDetalhes(): List<OrdemServicoEntity>
}
