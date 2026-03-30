package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ServicoCatalogoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ServicoCatalogoJpaRepository : JpaRepository<ServicoCatalogoEntity, String>
