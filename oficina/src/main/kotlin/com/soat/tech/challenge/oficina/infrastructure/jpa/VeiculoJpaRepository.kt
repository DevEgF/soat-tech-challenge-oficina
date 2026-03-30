package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VeiculoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VeiculoJpaRepository : JpaRepository<VeiculoEntity, String> {
	fun findByLicensePlate(licensePlate: String): VeiculoEntity?
	fun findByCustomer_Id(customerId: String): List<VeiculoEntity>
}
