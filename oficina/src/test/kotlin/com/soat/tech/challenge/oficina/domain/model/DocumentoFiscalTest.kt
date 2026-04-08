package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidTaxDocumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TaxDocumentTest {

	@Test
	fun `given formatted CPF when parse then digits normalized`() {
		val d = TaxDocument.parse("529.982.247-25")
		assertEquals("52998224725", d.digits)
	}

	@Test
	fun `given CNPJ when parse then accepts valid`() {
		val d = TaxDocument.parse("11.222.333/0001-81")
		assertEquals("11222333000181", d.digits)
	}

	@Test
	fun `given invalid CPF when parse then throws`() {
		assertFailsWith<InvalidTaxDocumentException> {
			TaxDocument.parse("11111111111")
		}
	}

	@Test
	fun `given wrong length when parse then throws`() {
		assertFailsWith<InvalidTaxDocumentException> {
			TaxDocument.parse("123")
		}
	}
}
