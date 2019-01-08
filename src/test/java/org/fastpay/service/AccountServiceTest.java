package org.fastpay.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.fastpay.service.PaymentStatus.CANCELLED;
import static org.fastpay.service.PaymentStatus.COMPLETED;
import static org.junit.Assert.assertEquals;

public class AccountServiceTest {

    private final String ORIGIN_ACCOUNT = "PL10000000000000000000";
    private final String DEST_ACCOUNT = "GB99999919000000999999";
    private final String CURRENCY = "GBP";
    private final BigDecimal BIG_BALANCE = new BigDecimal("10000");
    private final BigDecimal SMALL_BALANCE = new BigDecimal("30");
    private final String TXID = "00097fb3-3ddf-4f25-bdb4-4302ec24c2a4";

    private final BigDecimal TRANSFER_AMOUNT = new BigDecimal("99.99");

    private AccountService service;

    @Before
    public void setup() {
        service = new AccountServiceImpl();
    }

    @After
    public void cleanup() {

    }

    @Test
    public void shouldDeclineAuthorizationOnInvalidAccount() {
        PaymentStatus result = service.authorizePayment(ORIGIN_ACCOUNT, DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);
        assertEquals(PaymentStatus.INVALID_ACCOUNT, result);
    }

    @Test
    public void shouldDeclineAuthorizationOnInsufficientFunds() {
        // given
        service.createAccount(ORIGIN_ACCOUNT, CURRENCY);
        service.deposit(ORIGIN_ACCOUNT, SMALL_BALANCE, CURRENCY, TXID);

        // when
        PaymentStatus result = service.authorizePayment(ORIGIN_ACCOUNT, DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);

        // then
        assertEquals(PaymentStatus.DECLINED, result);
    }

    @Test
    public void shouldAuthorizeAmount() {
        // given
        prepareAccountWithBigBalance();

        // when
        PaymentStatus result = service.authorizePayment(ORIGIN_ACCOUNT, DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);

        // then
        BigDecimal balance = service.balance(ORIGIN_ACCOUNT, CURRENCY);
        assertEquals(PaymentStatus.AUTHORIZED, result);
        assertEquals(new BigDecimal("9900.01"), balance); // balance decreased
    }


    @Test
    public void shouldCancelAuthorizedPayment() {
        // given
        prepareAccountWithBigBalance();
        service.authorizePayment(ORIGIN_ACCOUNT, DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);

        // when
        PaymentStatus cancelled = service.cancelPayment(TXID);
        assertEquals(CANCELLED, cancelled);

        // then
        BigDecimal balance = service.balance(ORIGIN_ACCOUNT, CURRENCY);
        assertEquals(PaymentStatus.CANCELLED, cancelled);
        assertEquals(new BigDecimal("10000.00"), balance); // balance restored
    }

    @Test
    public void shouldDeclineDepositOnInvalidAccount() {
        PaymentStatus result = service.deposit(DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);
        assertEquals(PaymentStatus.DECLINED, result);
    }

    @Test
    public void shouldDepositAmount() {
        // given
        service.createAccount(DEST_ACCOUNT, CURRENCY);

        // when
        PaymentStatus result = service.deposit(DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);

        // then
        assertEquals(PaymentStatus.COMPLETED, result);
        BigDecimal balance = service.balance(DEST_ACCOUNT, CURRENCY);
        assertEquals(new BigDecimal("99.99"), balance); // balance increased
    }


    @Test
    public void shouldFinalizeAuthorizedPayment() {
        // given
        prepareAccountWithBigBalance();
        service.createAccount(DEST_ACCOUNT, CURRENCY);
        service.authorizePayment(ORIGIN_ACCOUNT, DEST_ACCOUNT, TRANSFER_AMOUNT, CURRENCY, TXID);

        // when
        PaymentStatus result = service.finalizePayment(TXID);
        assertEquals(COMPLETED, result);

        // then
        BigDecimal balance = service.balance(DEST_ACCOUNT, CURRENCY);
        assertEquals(new BigDecimal("99.99"), balance); // increased

        BigDecimal sourceBalance = service.balance(ORIGIN_ACCOUNT, CURRENCY);
        assertEquals(new BigDecimal("9900.01"), sourceBalance); // decreased
    }

    private void prepareAccountWithBigBalance() {
        service.createAccount(ORIGIN_ACCOUNT, CURRENCY);
        service.deposit(ORIGIN_ACCOUNT, BIG_BALANCE, CURRENCY, TXID);
    }

}