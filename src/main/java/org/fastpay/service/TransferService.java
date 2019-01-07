package org.fastpay.service;

import org.fastpay.entity.FilterParametersDto;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferDto;
import org.fastpay.entity.TransferStatus;

import java.util.List;

public interface TransferService {
    Transfer createTransfer(TransferDto transferData);

    List<Transfer> getTransfers(FilterParametersDto filterParams);

    Transfer getTransferDetails(String txId);

    TransferStatus cancelTransfer(String txId);

    TransferStatus executeTransfer(String txId);

    void cleanup();

}
