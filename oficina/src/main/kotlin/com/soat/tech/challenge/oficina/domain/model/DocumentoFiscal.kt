package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.DocumentoInvalidoException

/**
 * CPF ou CNPJ brasileiro, somente dígitos (linguagem ubíqua: documento do cliente).
 */
@JvmInline
value class DocumentoFiscal private constructor(val digitos: String) {

	init {
		require(digitos.all { it.isDigit() }) { "Documento deve conter apenas dígitos" }
	}

	companion object {
		fun parse(texto: String): DocumentoFiscal {
			val digitos = texto.filter { it.isDigit() }
			if (digitos.length == 11) {
				if (!validarCpf(digitos)) throw DocumentoInvalidoException("CPF inválido")
				return DocumentoFiscal(digitos)
			}
			if (digitos.length == 14) {
				if (!validarCnpj(digitos)) throw DocumentoInvalidoException("CNPJ inválido")
				return DocumentoFiscal(digitos)
			}
			throw DocumentoInvalidoException("Documento deve ter 11 (CPF) ou 14 (CNPJ) dígitos")
		}

		internal fun validarCpf(d: String): Boolean {
			if (d.length != 11 || d.all { it == d[0] }) return false
			fun digito(base: String, fatorInicial: Int): Int {
				var sum = 0
				for (i in base.indices) sum += base[i].digitToInt() * (fatorInicial - i)
				val r = sum % 11
				return if (r < 2) 0 else 11 - r
			}
			val d1 = digito(d.substring(0, 9), 10)
			val d2 = digito(d.substring(0, 9) + d1, 11)
			return d[9].digitToInt() == d1 && d[10].digitToInt() == d2
		}

		internal fun validarCnpj(d: String): Boolean {
			if (d.length != 14 || d.all { it == d[0] }) return false
			val w1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
			val w2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
			fun digito(base: String, weights: IntArray): Int {
				var sum = 0
				for (i in weights.indices) sum += base[i].digitToInt() * weights[i]
				val r = sum % 11
				return if (r < 2) 0 else 11 - r
			}
			val d1 = digito(d, w1)
			val d2 = digito(d.substring(0, 12) + d1, w2)
			return d[12].digitToInt() == d1 && d[13].digitToInt() == d2
		}
	}
}
