package org.fastpay.repository;

import org.fastpay.entity.FilterParametersDto;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferDto;

import java.util.List;

public interface TransferRepository {
    Transfer add(TransferDto transferData);

    Transfer update(String txId, TransferDto transferData);

    List<Transfer> list(FilterParametersDto filterParams);

    Transfer get(String txId);

    boolean delete(String txId);

    void cleanup();
}
