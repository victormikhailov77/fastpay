package org.fastpay.service;

import java.math.BigDecimal;

public class AccountServiceImpl implements AccountService {

    @Override
    public PaymentStatus authorizePayment(String source, String destination, BigDecimal amount, String currency, String id) {
        // method stub
        return PaymentStatus.AUTHORIZED;
    }

    @Override
    public PaymentStatus cancelPayment(String txId) {
        return PaymentStatus.CANCELLED;
    }

    @Override
    public PaymentStatus deposit(String destination, BigDecimal amount, String currency, String id) {
        return PaymentStatus.COMPLETED;
    }

    @Override
    public PaymentStatus finalizePayment(String id) {
        return PaymentStatus.COMPLETED;
    }
}
