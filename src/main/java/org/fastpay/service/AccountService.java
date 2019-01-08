package org.fastpay.service;

import java.math.BigDecimal;

public interface AccountService {
    PaymentStatus authorizePayment(String source, String destination, BigDecimal amount, String currency, String txID);

    PaymentStatus cancelPayment(String txId);

    PaymentStatus deposit(String destination, BigDecimal amount, String currency, String txId);

    PaymentStatus finalizePayment(String txId);

    BigDecimal balance(String accountNumber, String currency);

    void createAccount(String origin_account, String currency);
}
