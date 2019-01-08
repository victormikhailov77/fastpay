package org.fastpay.service;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// Operations on banking account
// in real life scenario, should be wrapper around bank API
public class AccountServiceImpl implements AccountService {

    private final Map<String, BigDecimal> accountStorage = new HashMap<>(); // current balance, per account nr
    private final Map<String, Pair<String, BigDecimal>> bufferStorage = new HashMap<>(); // for not finalized transactions
    private final Map<String, Pair<String, PaymentStatus>> transactions = new HashMap<>(); //

    public AccountServiceImpl() {
        initializeAccounts();
    } // default constructor - for unit tests

    // set initial balance
    private void initializeAccounts() {
        accountStorage.put("PL61109010140000071219812874", new BigDecimal(1000L));
        accountStorage.put("CZ6508000000192000145399", new BigDecimal(0L));
        accountStorage.put("US122000103040445550000000", new BigDecimal(50000L));
        accountStorage.put("DE0445999991232449999999", new BigDecimal(0L));
        accountStorage.put("RU099912012000000031399999999", new BigDecimal(10000000000L));
        accountStorage.put("BA0090909090339494494949", new BigDecimal(50000L));
    }

    // Block requested amount on the account, by decreasing balance. This is reversible operation
    @Override
    public PaymentStatus authorizePayment(String source, String destination, BigDecimal amount, String currency, String txId) {
        if (!accountStorage.containsKey(source)) {
            return PaymentStatus.INVALID_ACCOUNT; // account locked or not exist
        }

        if (balance(source, currency).compareTo(amount) == -1) {
            // insufficient funds
            return PaymentStatus.DECLINED;
        }
        BigDecimal originBalance = accountStorage.get(source);
        originBalance = originBalance.subtract(amount);
        accountStorage.put(source, originBalance);

        transactions.put(txId, Pair.of(source, PaymentStatus.AUTHORIZED)); // store account nr used in transaction
        bufferStorage.put(txId, Pair.of(destination, amount)); // store amount in temporary buffer

        return PaymentStatus.AUTHORIZED;
    }

    // Compensating transaction to authorizePayment
    @Override
    public PaymentStatus cancelPayment(String txId) {
        Pair<String, PaymentStatus> pair = transactions.get(txId);
        PaymentStatus status = pair.getValue();
        if (!status.equals(PaymentStatus.AUTHORIZED)) {
            return PaymentStatus.ERROR; // wrong state
        }

        String accountNumber = pair.getKey();
        BigDecimal compensatingAmount = bufferStorage.get(txId).getValue();
        bufferStorage.remove(txId);

        BigDecimal balance = accountStorage.get(accountNumber);
        compensatingAmount = balance.add(compensatingAmount);
        accountStorage.put(accountNumber, compensatingAmount);

        return PaymentStatus.CANCELLED;
    }

    // Deposit money to the account
    @Override
    public PaymentStatus deposit(String accountNumber, BigDecimal amount, String currency, String txId) {
        if (!accountStorage.containsKey(accountNumber)) {
            return PaymentStatus.DECLINED; // account locked?
        }

        BigDecimal balance = accountStorage.get(accountNumber);
        balance = balance.add(amount);
        accountStorage.put(accountNumber, balance);

        return PaymentStatus.COMPLETED;
    }

    @Override
    public PaymentStatus finalizePayment(String txId) {
        Pair<String, PaymentStatus> pair = transactions.get(txId);
        PaymentStatus status = pair.getValue();
        if (!status.equals(PaymentStatus.AUTHORIZED)) {
            return PaymentStatus.ERROR; // must be authorized first
        }

        String destinationAccount = bufferStorage.get(txId).getKey();
        BigDecimal depositAmount = bufferStorage.get(txId).getValue();
        return deposit(destinationAccount, depositAmount, "same", txId);
    }

    @Override
    public BigDecimal balance(String accountNumber, String currency) {
        return accountStorage.get(accountNumber);
    }

    @Override
    public void createAccount(String originAccount, String currency) {
        accountStorage.put(originAccount, new BigDecimal(0L));
    }

}
