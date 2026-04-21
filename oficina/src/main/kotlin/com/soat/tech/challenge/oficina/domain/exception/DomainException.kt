package com.soat.tech.challenge.oficina.domain.exception

open class DomainException(message: String) : RuntimeException(message)

class InvalidTaxDocumentException(message: String) : DomainException(message)

class InvalidLicensePlateException(message: String) : DomainException(message)

class InvalidStatusTransitionException(
	val currentStatus: String,
	val action: String,
) : DomainException("Invalid transition: status=$currentStatus, action=$action")

class InsufficientStockException(partId: String, requested: Int, available: Int) :
	DomainException("Insufficient stock for part $partId: requested=$requested, available=$available")
