package com.soat.tech.challenge.oficina.domain.port

interface NotificationPort {
    fun notifyWorkOrderFinalized(customerName: String, customerEmail: String?, vehicleModel: String)
    fun notifyQuoteSentToCustomer(customerName: String, customerEmail: String?, vehicleModel: String, quoteTotalCents: Long)
    fun notifyDeliveryRegistered(customerName: String, customerEmail: String?, vehicleModel: String)
    // expandir na Task 4 para outras transições
}