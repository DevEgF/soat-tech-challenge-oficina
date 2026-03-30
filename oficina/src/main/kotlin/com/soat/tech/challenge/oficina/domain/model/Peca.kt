package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

/** Peça ou insumo com controle de estoque. */
data class Peca(
	val id: UUID,
	val codigo: String,
	val nome: String,
	val precoCentavos: Long,
	val quantidadeEstoque: Int,
) {
	init {
		require(quantidadeEstoque >= 0) { "Estoque não pode ser negativo" }
	}

	fun comEstoqueAjustado(delta: Int): Peca {
		val novo = quantidadeEstoque + delta
		require(novo >= 0) { "Estoque resultante negativo" }
		return copy(quantidadeEstoque = novo)
	}
}
