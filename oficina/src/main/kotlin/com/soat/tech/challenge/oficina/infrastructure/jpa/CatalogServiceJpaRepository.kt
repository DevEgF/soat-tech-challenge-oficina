package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.CatalogServiceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CatalogServiceJpaRepository : JpaRepository<CatalogServiceEntity, String>
