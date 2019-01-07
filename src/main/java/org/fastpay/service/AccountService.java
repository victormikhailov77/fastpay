package org.fastpay.service;

import java.math.BigDecimal;

public interface AccountService {
    PaymentStatus authorizePayment(String source, String destination, BigDecimal amount, String currency, String id);

    PaymentStatus cancelPayment(String txId);

    PaymentStatus deposit(String destination, BigDecimal amount, String currency, String id);

    PaymentStatus finalizePayment(String id);
}
