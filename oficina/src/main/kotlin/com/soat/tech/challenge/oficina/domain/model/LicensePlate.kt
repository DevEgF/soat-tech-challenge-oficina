package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.InvalidLicensePlateException

/**
 * Legacy plate (ABC1234) or Mercosur (ABC1D23) — ubiquitous language: vehicle plate.
 */
@JvmInline
value class LicensePlate private constructor(val normalized: String) {

	companion object {
		private val LEGACY = Regex("^[A-Z]{3}[0-9]{4}$")
		private val MERCOSUR = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

		fun parse(text: String): LicensePlate {
			val n = text.uppercase().filter { it.isLetterOrDigit() }
			if (!LEGACY.matches(n) && !MERCOSUR.matches(n)) {
				throw InvalidLicensePlateException("Invalid plate: use ABC1234 or Mercosur ABC1D23")
			}
			return LicensePlate(n)
		}
	}
}
