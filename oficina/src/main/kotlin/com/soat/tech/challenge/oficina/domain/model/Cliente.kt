package com.soat.tech.challenge.oficina.domain.model

import java.util.UUID

data class Cliente(
	val id: UUID,
	val fiscalDocument: DocumentoFiscal,
	val name: String,
	val email: String? = null,
	val phone: String? = null,
)
