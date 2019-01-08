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
        TransferStatus status = filterParams.getStatus();
        String currency = filterParams.getCurrency();
        String sourceAccount = filterParams.getSource();
        String destinationAccount = filterParams.getDestination();
        String title = filterParams.getTitle();
        String sortPropertyName = filterParams.getSort();
        BigDecimal amount = filterParams.getAmount();
        Long limit = filterParams.getLimit();
        boolean isReverseOrder = filterParams.getOrder() != null && filterParams.getOrder().equals("desc") ? true : false;
        Comparator<Transfer> comparator = getSortComparator(sortPropertyName);
        Comparator<Transfer> orderedComparator = isReverseOrder ? comparator.reversed() : comparator;

        Stream<Transfer> stream = transferStore.values().stream()
                .filter(item -> status != null ? item.getStatus().equals(status) : true)
                .filter(item -> currency != null ? item.getCurrency().equals(currency) : true)
                .filter(item -> sourceAccount != null ? item.getSource().equals(sourceAccount) : true)
                .filter(item -> destinationAccount != null ? item.getDestination().equals(destinationAccount) : true)
                .filter(item -> title != null ? item.getTitle().equals(title) : true)
                .filter(item -> amount != null ? item.getAmount().equals(amount) : true)
                .sorted(sortPropertyName != null ? orderedComparator :
                        Comparator.comparing(Transfer::getTimestamp))
                .limit(limit != null ? limit : transferStore.values().size());

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
