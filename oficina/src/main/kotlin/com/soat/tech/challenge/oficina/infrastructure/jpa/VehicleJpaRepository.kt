package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VehicleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VehicleJpaRepository : JpaRepository<VehicleEntity, String> {
	fun findByLicensePlate(licensePlate: String): VehicleEntity?
	fun findByCustomer_Id(customerId: String): List<VehicleEntity>
}
