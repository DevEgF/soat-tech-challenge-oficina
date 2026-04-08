package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidLicensePlateException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LicensePlateTest {

	@Test
	fun `given legacy format when parse then normalized`() {
		assertEquals("ABC1234", LicensePlate.parse("abc-1234").normalized)
	}

	@Test
	fun `given Mercosur format when parse then normalized`() {
		assertEquals("ABC1D23", LicensePlate.parse("abc1d23").normalized)
	}

	@Test
	fun `given invalid pattern when parse then throws`() {
		assertFailsWith<InvalidLicensePlateException> {
			LicensePlate.parse("1234567")
		}
	}
}
