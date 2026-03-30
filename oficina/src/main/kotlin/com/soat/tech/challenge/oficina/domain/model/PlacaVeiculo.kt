package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.PlacaInvalidaException

/**
 * Placa no padrão antigo (ABC1234) ou Mercosul (ABC1D23) — linguagem ubíqua: placa do veículo.
 */
@JvmInline
value class PlacaVeiculo private constructor(val normalizada: String) {

	companion object {
		private val ANTIGA = Regex("^[A-Z]{3}[0-9]{4}$")
		private val MERCOSUL = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

		fun parse(texto: String): PlacaVeiculo {
			val n = texto.uppercase().filter { it.isLetterOrDigit() }
			if (!ANTIGA.matches(n) && !MERCOSUL.matches(n)) {
				throw PlacaInvalidaException("Placa inválida: use formato ABC1234 ou Mercosul ABC1D23")
			}
			return PlacaVeiculo(n)
		}
	}
}
