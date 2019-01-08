package org.fastpay.app;

import com.google.inject.AbstractModule;
import org.fastpay.Application;
import org.fastpay.repository.TransferRepository;
import org.fastpay.repository.TransferRepositoryImpl;
import org.fastpay.service.AccountService;
import org.fastpay.service.TransferService;
import org.fastpay.service.TransferServiceImpl;

import javax.inject.Singleton;

public class TestModule extends AbstractModule {

    private final AccountService originAccount;

    public TestModule(AccountService originAccount) {
        this.originAccount = originAccount;
    }

    @Override
    protected void configure() {
        bind(TransferService.class).to(TransferServiceImpl.class);
        bind(TransferRepository.class).to(TransferRepositoryImpl.class);
        bind(AccountService.class).toInstance(originAccount);
        bind(Application.class).in(Singleton.class);
    }

}