package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.PlacaInvalidaException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlacaVeiculoTest {

	@Test
	fun `aceita placa antiga`() {
		assertEquals("ABC1234", PlacaVeiculo.parse("abc-1234").normalizada)
	}

	@Test
	fun `aceita placa Mercosul`() {
		assertEquals("ABC1D23", PlacaVeiculo.parse("abc1d23").normalizada)
	}

	@Test
	fun `rejeita formato invalido`() {
		assertFailsWith<PlacaInvalidaException> {
			PlacaVeiculo.parse("1234ABC")
		}
	}
}
