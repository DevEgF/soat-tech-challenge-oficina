package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.DocumentoInvalidoException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentoFiscalTest {

	@Test
	fun `aceita CPF valido com formatacao`() {
		val d = DocumentoFiscal.parse("529.982.247-25")
		assertEquals("52998224725", d.digitos)
	}

	@Test
	fun `aceita CNPJ valido`() {
		val d = DocumentoFiscal.parse("11.222.333/0001-81")
		assertEquals("11222333000181", d.digitos)
	}

	@Test
	fun `rejeita CPF invalido`() {
		assertFailsWith<DocumentoInvalidoException> {
			DocumentoFiscal.parse("111.111.111-11")
		}
	}

	@Test
	fun `rejeita tamanho incorreto`() {
		assertFailsWith<DocumentoInvalidoException> {
			DocumentoFiscal.parse("123")
		}
	}
}
