package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "servicos_catalogo")
class ServicoCatalogoEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@Column(nullable = false, name = "nome")
	var name: String = "",
	@Column(length = 2000, name = "descricao")
	var description: String? = null,
	@Column(nullable = false, name = "preco_centavos")
	var priceCents: Long = 0,
	@Column(nullable = false, name = "tempo_estimado_minutos")
	var estimatedMinutes: Int = 0,
)
