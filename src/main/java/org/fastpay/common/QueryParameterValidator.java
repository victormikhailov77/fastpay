package org.fastpay.common;

import org.apache.commons.lang3.StringUtils;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferStatus;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class QueryParameterValidator {

    public static String getSortOrder(String sortOrder) {
        if (sortOrder == null) return null;

        Set<String> validValues = new HashSet<>(Arrays.asList("asc", "desc"));
        if (validValues.contains(sortOrder.toLowerCase())) {
            return sortOrder.toLowerCase();
        } else {
            throw new ParameterValidationException("Invalid value in query parameter 'order'. Accepted values are asc, desc");
        }
    }

    public static Long getNumberParameter(String numberAsString) {
        Long value = RestOperationTemplate.DEFAULT_QUERY_LIMIT;

        if (numberAsString != null) {
            try {
                value = Long.parseLong(numberAsString);
            } catch (NumberFormatException ex) {
                throw new ParameterValidationException("Invalid numeric value in query parameter 'limit'", ex);
            }
        }

        return value;
    }

    public static TransferStatus getTransferStatus(String status) {
        TransferStatus transferStatus = null;

        if (status != null) {
            try {
                transferStatus = TransferStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ParameterValidationException("Invalid value in query parameter 'status'", ex);
            }
        }

        return transferStatus;
    }

    public static String getCurrency(String currency) {
        if (currency == null) return null;

        String currencyUpper = currency.toUpperCase();
        Set<String> currencies = new HashSet<>(Arrays.asList("JPY", "CNY", "SDG", "RON", "MKD", "MXN", "CAD",
                "ZAR", "AUD", "NOK", "ILS", "ISK", "SYP", "LYD", "UYU", "YER", "CSD",
                "EEK", "THB", "IDR", "LBP", "AED", "BOB", "QAR", "BHD", "HNL", "HRK",
                "COP", "ALL", "DKK", "MYR", "SEK", "RSD", "BGN", "DOP", "KRW", "LVL",
                "VEF", "CZK", "TND", "KWD", "VND", "JOD", "NZD", "PAB", "CLP", "PEN",
                "GBP", "DZD", "CHF", "RUB", "UAH", "ARS", "SAR", "EGP", "INR", "PYG",
                "TWD", "TRY", "BAM", "OMR", "SGD", "MAD", "BYR", "NIO", "HKD", "LTL",
                "SKK", "GTQ", "BRL", "EUR", "HUF", "IQD", "CRC", "PHP", "SVC", "PLN",
                "USD"));

        if (!currencies.contains(currencyUpper)) {
            throw new ParameterValidationException("Invalid value in query parameter 'currency'");
        }

        return currencyUpper;
    }

    public static BigDecimal getMoney(String amount) {
        if (amount == null) return null;

        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException ex) {
            throw new ParameterValidationException("Invalid value in query parameter 'amount'");
        }
    }

    // comparator provides sorting on property name in Transfer class
    public static Comparator<Transfer> getSortComparator(String sortPropertyName) {
        if (sortPropertyName == null) return null;

        try {
            // translate field name to accessor to property
            final Method method = Transfer.class.getMethod("get" + StringUtils.capitalize(sortPropertyName));
            return (x, y) -> MethodGetWrapper.of(method, x).compareTo(MethodGetWrapper.of(method, y));
        } catch (NoSuchMethodException ex) {
            throw new ParameterValidationException("Invalid field name in query parameter 'sort'", ex);
        }
    }
}
