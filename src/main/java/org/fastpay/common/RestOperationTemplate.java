package org.fastpay.common;

import com.google.gson.Gson;
import org.fastpay.Application;
import org.fastpay.entity.*;
import org.fastpay.service.TransferService;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.fastpay.common.QueryParameterValidator.*;

// REST template uses Template method design pattern, to keep boilerplate code in one place
public class RestOperationTemplate {

    public static final String APPLICATION_TYPE_JSON = "application/json";
    public static final long DEFAULT_QUERY_LIMIT = 100L;
    private final TransferService transferService;

    public RestOperationTemplate(TransferService transferService) {
        this.transferService = transferService;
    }

    public String resourceUpdate(Request request, Response response, UpdateOperation operation,
                                 String errorMessage, String successMessage) {
        response.type(APPLICATION_TYPE_JSON);
        response.status(SC_OK);

        Transfer result = transferService.getTransferDetails(request.params(":id"));
        if (result != null) {
            TransferStatus status = operation.execute(result.getId());
            if (status.equals(TransferStatus.ERROR)) {
                response.status(SC_CONFLICT);
                return new Gson().toJson(new ServiceResponse(TransferStatus.ERROR, errorMessage,
                        new Gson().toJsonTree(result)));
            } else {
                return new Gson().toJson(new ServiceResponse(result.getStatus(), successMessage,
                        new Gson().toJsonTree(result)));
            }

        } else {
            response.status(SC_NOT_FOUND);
            return new Gson().toJson(new ServiceResponse(TransferStatus.ERROR, "No transfer with such id found",
                    null));

        }
    }

    public String resourceCreate(Request request, Response response, CreateOperation operation,
                                 String successMessage, String failureMessage) {
        response.type(APPLICATION_TYPE_JSON);
        try {
            TransferDto transferData = new Gson().fromJson(request.body(), TransferDto.class);
            Transfer newTransfer = operation.execute(transferData);
            response.status(SC_CREATED);
            return new Gson()
                    .toJson(new ServiceResponse(newTransfer.getStatus(), successMessage,
                            new Gson().toJsonTree(newTransfer)));
        } catch (Exception ex) {
            LoggerFactory.getLogger(Application.class).error("Exception occurred during resource creation", ex);
            response.status(SC_INTERNAL_SERVER_ERROR);
            return new Gson().toJson(new ServiceResponse(TransferStatus.ERROR, failureMessage,
                    new Gson().toJsonTree(ex.getMessage())));
        }

    }

    public String resourceGetAll(Request request, Response response, GetAllOperation operation) {
        response.type(APPLICATION_TYPE_JSON);
        response.status(SC_OK);
        try {
            FilterParametersDto filterParams = new FilterParametersDto();
            filterParams.setLimit(getNumberParameter(request.queryParams("limit")));
            filterParams.setSort(request.queryParams("sort"));
            filterParams.setOrder(getSortOrder(request.queryParams("order")));
            filterParams.setStatus(getTransferStatus(request.queryParams("status")));
            filterParams.setCurrency(getCurrency(request.queryParams("currency")));
            filterParams.setSource(request.queryParams("source"));
            filterParams.setTitle(request.queryParams("title"));
            filterParams.setAmount(getMoney(request.queryParams("amount")));
            return new Gson().toJson(operation.execute(filterParams));
        } catch (ParameterValidationException ex) {
            LoggerFactory.getLogger(Application.class).error("Invalid parameter. " + ex.getMessage(), ex);
            response.status(SC_BAD_REQUEST);
            return new Gson().toJson(new ServiceResponse(TransferStatus.ERROR, ex.getMessage(),
                    null));
        }
    }

    public String resourceGet(Request request, Response response, GetOperation operation,
                              String successMessage, String notFoundMessage) {
        response.type(APPLICATION_TYPE_JSON);
        response.status(SC_OK);

        Transfer result = operation.execute(request.params(":id"));
        if (result != null) {
            return new Gson().toJson(new ServiceResponse(result.getStatus(), successMessage,
                    new Gson().toJsonTree(result)));

        } else {
            response.status(SC_NOT_FOUND);
            return new Gson().toJson(new ServiceResponse(TransferStatus.ERROR, notFoundMessage,
                    null));
        }
    }

    public Object resourceCleanup(Runnable cleanup) {
        cleanup.run();
        return null;
    }

    @FunctionalInterface
    public interface UpdateOperation {
        TransferStatus execute(String id);
    }

    @FunctionalInterface
    public interface CreateOperation {
        Transfer execute(TransferDto obj);
    }

    @FunctionalInterface
    public interface GetAllOperation {
        List<Transfer> execute(FilterParametersDto filterParams);
    }

    @FunctionalInterface
    public interface GetOperation {
        Transfer execute(String id);
    }
}
