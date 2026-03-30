package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidTaxDocumentException

/**
 * Brazilian CPF or CNPJ, digits only (ubiquitous language: customer tax document).
 */
@JvmInline
value class DocumentoFiscal private constructor(val digits: String) {

	init {
		require(digits.all { it.isDigit() }) { "Document must contain only digits" }
	}

	companion object {
		fun parse(text: String): DocumentoFiscal {
			val d = text.filter { it.isDigit() }
			if (d.length == 11) {
				if (!validateCpf(d)) throw InvalidTaxDocumentException("Invalid CPF")
				return DocumentoFiscal(d)
			}
			if (d.length == 14) {
				if (!validateCnpj(d)) throw InvalidTaxDocumentException("Invalid CNPJ")
				return DocumentoFiscal(d)
			}
			throw InvalidTaxDocumentException("Document must have 11 (CPF) or 14 (CNPJ) digits")
		}

		internal fun validateCpf(d: String): Boolean {
			if (d.length != 11 || d.all { it == d[0] }) return false
			fun checkDigit(base: String, initialFactor: Int): Int {
				var sum = 0
				for (i in base.indices) sum += base[i].digitToInt() * (initialFactor - i)
				val r = sum % 11
				return if (r < 2) 0 else 11 - r
			}
			val d1 = checkDigit(d.substring(0, 9), 10)
			val d2 = checkDigit(d.substring(0, 9) + d1, 11)
			return d[9].digitToInt() == d1 && d[10].digitToInt() == d2
		}

		internal fun validateCnpj(d: String): Boolean {
			if (d.length != 14 || d.all { it == d[0] }) return false
			val w1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
			val w2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
			fun checkDigit(base: String, weights: IntArray): Int {
				var sum = 0
				for (i in weights.indices) sum += base[i].digitToInt() * weights[i]
				val r = sum % 11
				return if (r < 2) 0 else 11 - r
			}
			val d1 = checkDigit(d, w1)
			val d2 = checkDigit(d.substring(0, 12) + d1, w2)
			return d[12].digitToInt() == d1 && d[13].digitToInt() == d2
		}
	}
}
