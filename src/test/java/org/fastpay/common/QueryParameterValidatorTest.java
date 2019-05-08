package org.fastpay.common;

import org.fastpay.entity.TransferStatus;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class QueryParameterValidatorTest {

    private QueryParameterValidator validator = new QueryParameterValidator();

    @Test
    public void shouldGetSortOrderAsc() {
        assertEquals("asc", validator.getSortOrder("asc"));
    }

    @Test
    public void shouldGetSortOrderDesc() {
        assertEquals("desc", validator.getSortOrder("desc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidSortOrder() {
        validator.getSortOrder("other");
    }

    @Test
    public void shouldGetNumberParameter() {
        assertEquals(new Long(1L), validator.getNumberParameter("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidNumberParameter() {
        validator.getNumberParameter("1A");
    }

    @Test
    public void shouldGetTransferStatus() {
        assertEquals(TransferStatus.PENDING, validator.getTransferStatus("pending"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidTransferStatus() {
        validator.getTransferStatus("nothing");
    }

    @Test
    public void shouldGetCurrency() {
        assertEquals("USD", validator.getCurrency("usd"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidCurrency() {
        validator.getCurrency("xyz");
    }

    @Test
    public void shouldGetMoney() {
        assertEquals(new BigDecimal("1.84"), validator.getMoney("1.84"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidMoney() {
        validator.getMoney("1.84A");
    }

    @Test
    public void shouldGetSortComparator() {
        validator.getSortComparator("status");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidSortComparator() {
        validator.getSortComparator("age");
    }
}