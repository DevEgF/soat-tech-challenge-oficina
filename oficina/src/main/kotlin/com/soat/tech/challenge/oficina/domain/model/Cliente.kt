package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class Cliente(
	val id: UUID,
	val documento: DocumentoFiscal,
	val nome: String,
	val email: String? = null,
	val telefone: String? = null,
)
