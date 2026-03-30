package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

/** Serviço cadastrado na oficina (catálogo) com preço e tempo estimado de execução. */
data class ServicoCatalogo(
	val id: UUID,
	val nome: String,
	val descricao: String? = null,
	val precoCentavos: Long,
	val tempoEstimadoMinutos: Int,
)
