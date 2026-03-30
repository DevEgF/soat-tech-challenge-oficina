package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VeiculoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VeiculoJpaRepository : JpaRepository<VeiculoEntity, String> {
	fun findByPlaca(placa: String): VeiculoEntity?
	fun findByCliente_Id(clienteId: String): List<VeiculoEntity>
}
