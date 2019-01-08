package org.fastpay;

import com.google.inject.Guice;
import org.fastpay.app.GuiceModule;
import org.fastpay.common.LogHelper;
import org.fastpay.common.RestOperationTemplate;
import org.fastpay.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static spark.Spark.*;

/**
 * Money transfer REST API implementation
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
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

        logger.info("Server started");

        // return newly created transfer with assigned id
        post(RESOURCE_URI, (request, response) ->
            restTemplate.resourceCreate(request, response, transferService::createTransfer,
                    "Transfer successfully created", "Error occured. Transfer not created.")
        );

        // return list of all transfers
        get(RESOURCE_URI, (request, response) ->
            restTemplate.resourceGetAll(request, response, transferService::getTransfers)
        );

        // return transfer details
        get(RESOURCE_URI_ID, (request, response) ->
            restTemplate.resourceGet(request, response, transferService::getTransferDetails,
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
            restTemplate.resourceCleanup(transferService::cleanup)
        );

        after((request, response) -> {
            logger.info(LogHelper.requestToString(request));
            logger.info(LogHelper.responseToString(response));
        });

    }

}
