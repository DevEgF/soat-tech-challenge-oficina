package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidTaxDocumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentoFiscalTest {

	@Test
	fun `given formatted CPF when parse then digits normalized`() {
		val d = DocumentoFiscal.parse("529.982.247-25")
		assertEquals("52998224725", d.digits)
	}

	@Test
	fun `given CNPJ when parse then accepts valid`() {
		val d = DocumentoFiscal.parse("11.222.333/0001-81")
		assertEquals("11222333000181", d.digits)
	}

	@Test
	fun `given invalid CPF when parse then throws`() {
		assertFailsWith<InvalidTaxDocumentException> {
			DocumentoFiscal.parse("11111111111")
		}
	}

	@Test
	fun `given wrong length when parse then throws`() {
		assertFailsWith<InvalidTaxDocumentException> {
			DocumentoFiscal.parse("123")
		}
	}
}
