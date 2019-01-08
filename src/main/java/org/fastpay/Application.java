package org.fastpay;

import com.google.inject.Guice;
import lombok.extern.slf4j.Slf4j;
import org.fastpay.app.GuiceModule;
import org.fastpay.common.LogHelper;
import org.fastpay.common.RestOperationTemplate;
import org.fastpay.service.TransferService;

import javax.inject.Inject;

import static spark.Spark.*;

/**
 * Money transfer REST API implementation
 */
@Slf4j
public class Application {

    private static final String RESOURCE_URI = "/transfer";
    private static final String RESOURCE_URI_ID = RESOURCE_URI + "/:id";
    private static final String RESOURCE_URI_CLEANUP = "/deleteAllTransfers";
    private static final int DEFAULT_PORT = 4567;

    private final TransferService transferService;
    private final RestOperationTemplate restTemplate;

    @Inject
    Application(final TransferService transferService) {
        this.transferService = transferService;
        this.restTemplate = new RestOperationTemplate(transferService);
    }

    public static void main(final String... args) {
        Guice.createInjector(new GuiceModule())
                .getInstance(Application.class)
                .run(DEFAULT_PORT);
    }

    void run(final int port) {
        port(port);

        log.info("Server started");

        // return newly created transfer with assigned id
        post(RESOURCE_URI, (request, response) ->
            restTemplate.resourceCreate(request, response,
                    "Transfer successfully created", "Error occured. Transfer not created.")
        );

        // return list of all transfers
        get(RESOURCE_URI, (request, response) ->
            restTemplate.resourceGetAll(request, response)
        );

        // return transfer details
        get(RESOURCE_URI_ID, (request, response) ->
            restTemplate.resourceGet(request, response,
                    "Transfer details successfully retrieved", "No transfer with such id found")
        );

        // execute transfer request; must be in pending state
        put(RESOURCE_URI_ID, (request, response) ->
            restTemplate.resourceUpdate(request, response, transferService::executeTransfer,
                    "Transfer execution not possible", "Transfer executed")
        );

        // cancel request; only pending can be cancelled
        delete(RESOURCE_URI_ID, (request, response) ->
            restTemplate.resourceUpdate(request, response, transferService::cancelTransfer,
                    "Transfer cancellation not possible", "Transfer cancelled")
        );

        // cleanup database
        // maintenance function, should be not available for users
        delete(RESOURCE_URI_CLEANUP, (request, response) ->
            restTemplate.resourceCleanup()
        );

        after((request, response) -> {
            log.info(LogHelper.requestToString(request));
            log.info(LogHelper.responseToString(response));
        });

    }

}
