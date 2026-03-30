package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "clientes")
class ClienteEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@Column(nullable = false, unique = true, length = 14)
	var documento: String = "",
	@Column(nullable = false)
	var nome: String = "",
	@Column
	var email: String? = null,
	@Column(length = 50)
	var telefone: String? = null,
)
