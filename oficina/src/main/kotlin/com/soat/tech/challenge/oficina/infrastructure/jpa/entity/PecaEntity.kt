package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "pecas")
class PecaEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@Column(nullable = false, unique = true, length = 100, name = "codigo")
	var code: String = "",
	@Column(nullable = false, name = "nome")
	var name: String = "",
	@Column(nullable = false, name = "preco_centavos")
	var priceCents: Long = 0,
	@Column(nullable = false, name = "quantidade_estoque")
	var stockQuantity: Int = 0,
	@Column(name = "ponto_reposicao")
	var replenishmentPoint: Int? = null,
)
