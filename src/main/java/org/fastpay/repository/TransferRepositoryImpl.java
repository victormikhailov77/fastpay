package org.fastpay.repository;

import org.fastpay.entity.FilterParametersDto;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferDto;
import org.fastpay.entity.TransferStatus;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fastpay.common.QueryParameterValidator.getSortComparator;

// Super-lightweight in-memory NoSQL database, backed by key-value map
@Singleton
public class TransferRepositoryImpl implements TransferRepository {

    private Map<String, Transfer> transferStore = new HashMap<>();

    @Override
    public Transfer add(TransferDto transferData) {
        Transfer newTransfer = new Transfer();
        UUID uuid = UUID.randomUUID();
        newTransfer.setId(uuid.toString());
        newTransfer.setTimestamp(new Date());
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setSource(transferData.getSource());
        newTransfer.setDestination(transferData.getDestination());
        newTransfer.setAmount(transferData.getAmount());
        newTransfer.setCurrency(transferData.getCurrency());
        newTransfer.setTitle(transferData.getTitle());
        transferStore.put(newTransfer.getId(), newTransfer);
        return newTransfer;
    }

    @Override
    public Transfer update(String txId, TransferDto transferData) {
        Transfer updated = transferStore.get(txId);
        if (updated != null) {
            updated.setSource(transferData.getSource());
            updated.setDestination(transferData.getDestination());
            updated.setAmount(transferData.getAmount());
            updated.setCurrency(transferData.getCurrency());
            updated.setTitle(transferData.getTitle());
        }
        return updated;
    }


    @Override
    public List<Transfer> list(FilterParametersDto filterParams) {
        TransferStatus statusVal = filterParams.getStatus();
        String currencyVal = filterParams.getCurrency();
        String sourceAccountVal = filterParams.getSource();
        String destinationAccountVal = filterParams.getDestination();
        String titleVal = filterParams.getTitle();
        String sortPropertyName = filterParams.getSort();
        BigDecimal amountVal = filterParams.getAmount();
        boolean orderDesc = filterParams.getOrder() != null && filterParams.getOrder().equals("desc") ? true : false;
        Long limitVal = filterParams.getLimit();
        Comparator<Transfer> comparator = getSortComparator(sortPropertyName);

        Stream<Transfer> stream = transferStore.values().stream()
                .filter(item -> statusVal != null ? item.getStatus().equals(statusVal) : true)
                .filter(item -> currencyVal != null ? item.getCurrency().equals(currencyVal) : true)
                .filter(item -> sourceAccountVal != null ? item.getSource().equals(sourceAccountVal) : true)
                .filter(item -> destinationAccountVal != null ? item.getDestination().equals(destinationAccountVal) : true)
                .filter(item -> titleVal != null ? item.getTitle().equals(titleVal) : true)
                .filter(item -> amountVal != null ? item.getAmount().equals(amountVal) : true)
                .sorted(sortPropertyName != null ?
                        (orderDesc ? comparator.reversed() :
                                comparator) :
                        Comparator.comparing(Transfer::getTimestamp))
                .limit(limitVal != null ? limitVal : transferStore.values().size());

        return stream.collect(Collectors.toList());
    }

    @Override
    public Transfer get(String txId) {
        return transferStore.get(txId);
    }

    @Override
    public boolean delete(String txId) {
        return transferStore.remove(txId) != null;
    }

    @Override
    public void cleanup() {
        transferStore.clear();
    }
}
