package org.fastpay.service;

import org.fastpay.common.PaymentAuthorizationException;
import org.fastpay.entity.FilterParametersDto;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferDto;
import org.fastpay.entity.TransferStatus;
import org.fastpay.repository.TransferRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

// Business logic implementation class
@Singleton
public class TransferServiceImpl implements TransferService {

    private final TransferRepository repository;
    private final AccountService senderAccountService;
    private final AccountService recipientAccountService;

    @Inject
    public TransferServiceImpl(TransferRepository repository, AccountService senderAccountService,
                               AccountService recipientAccountService) {
        this.repository = repository;
        this.senderAccountService = senderAccountService;
        this.recipientAccountService = recipientAccountService;
    }

    @Override
    public Transfer createTransfer(TransferDto transferData) {

        Transfer newTransfer = repository.add(transferData);
        // authorize payment from source bank, which locks amount on the account
        // operation may fail for different reasons: insufficient funds, account blocked

        PaymentStatus status = senderAccountService.authorizePayment(newTransfer.getSource(), newTransfer.getDestination(), newTransfer.getAmount(),
                newTransfer.getCurrency(), newTransfer.getId());

        if (!status.equals(PaymentStatus.AUTHORIZED)) {
            // if operation declined - delete transfer (compensation transaction)
            repository.delete(newTransfer.getId());
            throw new PaymentAuthorizationException("Payment declined by sender's bank");
        }

        return newTransfer;
    }

    @Override
    public List<Transfer> getTransfers(FilterParametersDto filterParams) {
        return repository.list(filterParams);
    }

    @Override
    public Transfer getTransferDetails(String txId) {
        return repository.get(txId);
    }

    @Override
    public TransferStatus cancelTransfer(String txId) {
        Transfer transfer = getTransferDetails(txId);
        if (transfer != null && transfer.getStatus().equals(TransferStatus.PENDING)) {

            // if payment authorized, but not sent to the recipient,
            // its possible to cancel payment and revert transaction
            // account balance will be restored to previous state
            PaymentStatus status = senderAccountService.cancelPayment(txId);
            if (status.equals(PaymentStatus.CANCELLED)) {
                transfer.setStatus(TransferStatus.CANCELLED);
                repository.update(txId, transfer);
                return TransferStatus.CANCELLED;
            }
        }
        return TransferStatus.ERROR;

    }

    @Override
    public TransferStatus executeTransfer(String txId) {
        Transfer transfer = getTransferDetails(txId);
        if (transfer != null && transfer.getStatus().equals(TransferStatus.PENDING)) {
            // deposit money to recipient account
            PaymentStatus status = recipientAccountService.deposit(transfer.getDestination(), transfer.getAmount(),
                    transfer.getCurrency(), transfer.getId());

            if (status.equals(PaymentStatus.COMPLETED)) {
                PaymentStatus senderStatus = senderAccountService.finalizePayment(transfer.getId());
                if (senderStatus.equals(PaymentStatus.COMPLETED)) {
                    transfer.setStatus(TransferStatus.COMPLETED);
                    repository.update(txId, transfer);
                    return TransferStatus.COMPLETED;
                } else {
                    // still authorized/pending
                    return TransferStatus.PENDING;
                }

            }
        }
        return TransferStatus.ERROR;
    }

    @Override
    public void cleanup() {
        repository.cleanup();
    }
}
