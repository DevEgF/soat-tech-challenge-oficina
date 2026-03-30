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
	@Column(nullable = false)
	var nome: String = "",
	@Column(length = 2000)
	var descricao: String? = null,
	@Column(nullable = false, name = "preco_centavos")
	var precoCentavos: Long = 0,
	@Column(nullable = false, name = "tempo_estimado_minutos")
	var tempoEstimadoMinutos: Int = 0,
)
