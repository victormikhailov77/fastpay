package org.fastpay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Guice;
import org.fastpay.app.TestModule;
import org.fastpay.common.TestBase;
import org.fastpay.entity.ServiceResponse;
import org.fastpay.entity.Transfer;
import org.fastpay.entity.TransferStatus;
import org.fastpay.service.AccountService;
import org.fastpay.service.PaymentStatus;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

/**
 * Integration test - the whole REST API
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest extends TestBase {

    public static final int DEFAULT_PORT = 4567;

    @BeforeClass
    public static void startSpark() {

        AccountService account = mock(AccountService.class);
        when(account.authorizePayment(anyString(), anyString(), anyObject(), anyString(), anyString())).thenReturn(PaymentStatus.AUTHORIZED);
        when(account.cancelPayment(anyString())).thenReturn(PaymentStatus.CANCELLED);
        when(account.finalizePayment(anyString())).thenReturn(PaymentStatus.COMPLETED);
        when(account.deposit(anyString(), anyObject(), anyString(), anyString())).thenReturn(PaymentStatus.COMPLETED);

        Application instance = Guice.createInjector(new TestModule(account))
                .getInstance(Application.class);

        instance.run(DEFAULT_PORT);

        awaitInitialization();
    }

    @AfterClass
    public static void stopSpark() {
        stop();
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        // delete all database
        submitRequest("DELETE", "/deleteAllTransfers");
    }

    @Test
    public void shouldCreateTransfer() {
        // given
        String createJson = loadFileFromResource("create.json");

        // when
        String jsonResult = submitRequest("POST", "/transfer", createJson);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.PENDING, response.getStatus());
        assertEquals("Transfer successfully created", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(TransferStatus.PENDING, newTransfer.getStatus());
        assertEquals("PL61109010140000071219812874", newTransfer.getSource());
        assertEquals("CZ6508000000192000145399", newTransfer.getDestination());
        assertEquals(new BigDecimal("340.23"), newTransfer.getAmount());
        assertEquals("PLN", newTransfer.getCurrency());
        assertEquals("przełew własny", newTransfer.getTitle());
    }

    @Test
    public void shouldFailOnCreateWithInvalidAmount() {
        // given
        String createJson = loadFileFromResource("bad_amount.json");

        // when
        String jsonResult = submitRequest("POST", "/transfer", createJson);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.ERROR, response.getStatus());
        assertEquals("Error occured. Transfer not created.", response.getMessage());
    }

    @Test
    public void shouldFailOnCreateWithInvalidCurrency() {
        // given
        String createJson = loadFileFromResource("bad_currency.json");

        // when
        String jsonResult = submitRequest("POST", "/transfer", createJson);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.ERROR, response.getStatus());
        assertEquals("Error occured. Transfer not created.", response.getMessage());
    }

    @Test
    public void shouldGetTransferById() {
        // given
        Transfer transfer = createTransfer("create.json");
        String id = transfer.getId();

        // when
        String jsonResult = submitRequest("GET", "/transfer/" + id);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.PENDING, response.getStatus());
        assertEquals("Transfer details successfully retrieved", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(id, newTransfer.getId());
        assertEquals(TransferStatus.PENDING, newTransfer.getStatus());
        assertEquals("PL61109010140000071219812874", newTransfer.getSource());
        assertEquals("CZ6508000000192000145399", newTransfer.getDestination());
        assertEquals(new BigDecimal("340.23"), newTransfer.getAmount());
        assertEquals("PLN", newTransfer.getCurrency());
        assertEquals("przełew własny", newTransfer.getTitle());

    }


    @Test
    public void shouldGetAllTransfers() {
        // given
        Transfer transfer = createTransfer("create.json");
        Transfer transfer2 = createTransfer("create2.json");
        Transfer transfer3 = createTransfer("create3.json");
        Set<String> sourceIds = new TreeSet<>(Arrays.asList(transfer.getId(), transfer2.getId(), transfer3.getId()));

        // when
        String jsonResult = submitRequest("GET", "/transfer");
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        Set<String> retrievedIds = new TreeSet<>();
        retrievedIds.addAll(allTransfers.stream().map(Transfer::getId).collect(Collectors.toList()));

        // then
        assertThat(sourceIds, is(retrievedIds));
    }

    @Test
    public void shouldGetTransfersSortedByCurrency() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sort", "currency");
        parameters.put("order", "asc");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<String> retrievedCurrenies = allTransfers.stream().map(Transfer::getCurrency).collect(Collectors.toList());

        // then
        assertEquals("PLN", retrievedCurrenies.get(0));
    }

    @Test
    public void shouldGetTransfersSortedByCurrencyReversedLimited() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sort", "currency");
        parameters.put("order", "desc");
        parameters.put("limit", "2");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<String> retrievedCurrenies = allTransfers.stream().map(Transfer::getCurrency).collect(Collectors.toList());

        // then
        assertEquals(2, retrievedCurrenies.size());
        assertEquals("USD", retrievedCurrenies.get(0));
    }

    @Test
    public void shouldGetTransfersSortedByAmount() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sort", "amount");
        parameters.put("limit", "1");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<BigDecimal> retrievedAmounts = allTransfers.stream().map(Transfer::getAmount).collect(Collectors.toList());

        // then
        assertEquals(1, retrievedAmounts.size());
        assertEquals(new BigDecimal("340.23"), retrievedAmounts.get(0));
    }

    @Test
    public void shouldGetTransfersSortedByTitle() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sort", "title");
        parameters.put("limit", "1");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<String> retrievedTitles = allTransfers.stream().map(Transfer::getTitle).collect(Collectors.toList());

        // then
        assertEquals(1, retrievedTitles.size());
        assertEquals("przełew własny", retrievedTitles.get(0));
    }

    @Test
    public void shouldFailOnInvalidSortField() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sort", "address");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);
        assertEquals(TransferStatus.ERROR, response.getStatus());
        assertEquals("Invalid field name in query parameter 'sort'", response.getMessage());
    }


    @Test
    public void shouldFilterTransfersByCurrency() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("currency", "USD");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<String> retrievedCurrenies = allTransfers.stream().map(Transfer::getCurrency).collect(Collectors.toList());

        // then
        assertEquals(1, retrievedCurrenies.size());
        assertEquals("USD", retrievedCurrenies.get(0));
    }

    @Test
    public void shouldFilterTransfersByAmount() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("amount", "340.23");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<BigDecimal> retrievedAmounts = allTransfers.stream().map(Transfer::getAmount).collect(Collectors.toList());

        // then
        assertEquals(1, retrievedAmounts.size());
        assertEquals(new BigDecimal("340.23"), retrievedAmounts.get(0));
    }

    @Test
    public void shouldFilterTransfersBySourceAccount() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("source", "US122000103040445550000000");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<String> retrievedAccountNrs = allTransfers.stream().map(Transfer::getSource).collect(Collectors.toList());

        // then
        assertEquals(1, retrievedAccountNrs.size());
        assertEquals("US122000103040445550000000", retrievedAccountNrs.get(0));
    }

    @Test
    public void shouldFilterTransfersByStatus() {
        // given
        createAllTransfers();

        // when
        Map<String, String> parameters = new HashMap<>();
        parameters.put("status", "PeNding");
        String jsonResult = submitRequest("GET", "/transfer", parameters);

        // then
        Type listType = new TypeToken<ArrayList<Transfer>>() {
        }.getType();
        List<Transfer> allTransfers = new Gson().fromJson(jsonResult, listType);
        List<TransferStatus> retrievedStatuses = allTransfers.stream().map(Transfer::getStatus).collect(Collectors.toList());

        // then
        assertEquals(3, retrievedStatuses.size());
        assertEquals(TransferStatus.PENDING, retrievedStatuses.get(0));
    }

    @Test
    public void shouldCancelCreatedTransfer() {
        // given
        Transfer transfer = createTransfer("create.json");
        String id = transfer.getId();

        // when
        // cancel by id
        String jsonResult = submitRequest("DELETE", "/transfer/" + id);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.CANCELLED, response.getStatus());
        assertEquals("Transfer cancelled", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(id, newTransfer.getId());
        assertEquals(TransferStatus.CANCELLED, newTransfer.getStatus());
    }

    @Test
    public void shouldExecuteCreatedTransfer() {
        // given
        Transfer transfer = createTransfer("create.json");
        String id = transfer.getId();

        // when
        // execute by id
        String jsonResult = submitRequest("PUT", "/transfer/" + id);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.COMPLETED, response.getStatus());
        assertEquals("Transfer executed", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(id, newTransfer.getId());
        assertEquals(TransferStatus.COMPLETED, newTransfer.getStatus());
    }

    @Test
    public void shouldFailOnCancelCompletedTransfer() {
        // given
        Transfer transfer = createTransfer("create.json");
        String id = transfer.getId();

        // execute by id
        submitRequest("PUT", "/transfer/" + id);

        // when
        // cancel - expect fail
        String jsonResult = submitRequest("DELETE", "/transfer/" + id);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.ERROR, response.getStatus());
        assertEquals("Transfer cancellation not possible", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(id, newTransfer.getId());
        assertEquals(TransferStatus.COMPLETED, newTransfer.getStatus());
    }

    @Test
    public void shouldFailOnExecuteCancelledTransfer() {
        // given
        Transfer transfer = createTransfer("create.json");
        String id = transfer.getId();

        // cancel by id
        submitRequest("DELETE", "/transfer/" + id);

        // when
        // execute - expect fail
        String jsonResult = submitRequest("PUT", "/transfer/" + id);
        ServiceResponse response = new Gson().fromJson(jsonResult, ServiceResponse.class);

        // then
        assertEquals(TransferStatus.ERROR, response.getStatus());
        assertEquals("Transfer execution not possible", response.getMessage());
        Transfer newTransfer = new Gson().fromJson(response.getData(), Transfer.class);
        assertEquals(id, newTransfer.getId());
        assertEquals(TransferStatus.CANCELLED, newTransfer.getStatus());
    }


    private Transfer createTransfer(String fileName) {
        String createJson = loadFileFromResource(fileName);
        String jsonResult = submitRequest("POST", "/transfer", createJson);
        ServiceResponse jsonResponse = new Gson().fromJson(jsonResult, ServiceResponse.class);
        Transfer newTransfer = new Gson().fromJson(jsonResponse.getData(), Transfer.class);
        return newTransfer;
    }

    private void createAllTransfers() {
        createTransfer("create.json");
        createTransfer("create2.json");
        createTransfer("create3.json");
    }
}
