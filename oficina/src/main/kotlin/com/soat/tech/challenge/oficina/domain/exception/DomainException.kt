package com.soat.tech.challenge.oficina.domain.exception

open class DomainException(message: String) : RuntimeException(message)

class DocumentoInvalidoException(message: String) : DomainException(message)

class PlacaInvalidaException(message: String) : DomainException(message)

class TransicaoStatusInvalidaException(
	val statusAtual: String,
	val acao: String,
) : DomainException("Transição inválida: status=$statusAtual, ação=$acao")

class EstoqueInsuficienteException(pecaId: String, solicitado: Int, disponivel: Int) :
	DomainException("Estoque insuficiente da peça $pecaId: solicitado=$solicitado, disponível=$disponivel")
