# fastpay
Money transfer REST API

Demo application, written in Java 8

Prerequisites:
Java 8 SDK (built on 1.8.0152)
Maven 3.6 or 3.3

Java libraries used:
Spark microframework (not Apache Spark!) - REST api
Gson - JSON processing
Lombok - get rid of getters/setters hell in POJOs
Guice - dependency injection
Apache Commons - various utilities
Mockito - unit tests

No Spring, Hibernate, or other heavy frameworks :-)  it starts instantly

No real database.
Application uses internal lightweight and superfast in-memory NoSQL data storage,
with sorting and filtering support.
Can be migrated to distributed key-value store or NoSQL database, like EHCache, Redis, Hazelcast, Cassandra

Limitations of functionality:

Due to lack of business requirements, application design and business logic do not cover real-life scenarios.

Sender and receiver identified by bank account numbers, which unique identify physical person or company. 
For simplicity, objects used in API don't have name, address data, other properties, etc - only account number used as primary identifier. 

-No multi-currency operation. Should be currency conversion, in cases:
  -currency of transfer different from currency of sender's account
  -currency of transfer dofferent from currency of recipient's account
  currency rates can be obtained from some web service (OpenExchange API, CurrencyLayer API)
  
-No commission calculation. Can be a per-transaction commission, based on bank location: 
ex: sender and recipient in the same bank - zero commission
    sender and recipient in different banks, same country - 1%
    sender and recipient both in EU economical zone - 2,5%
    sender and recipient in arbitrary countries without special agreements - 4%

How to build:

1) download sources

git clone https://github.com/victormikhailov77/fastpay.git


2) build

mvn clean install

3) run unit tests

mvn test


4) run standalone

java -jar target/moneyapp-1.0-SNAPSHOT-jar-with-dependencies.jar

application starts as standalone Jetty server, on port 4567

to test REST API endpoints, one can use curl, Chrome browser plugin (Restlet), or Postman 


REST API endpoints test

1) Create transfer

POST:  localhost:4567/transfer

curl -d '{"source":"PL61109010140000071219812874","destination":"CZ6508000000192000145399","amount":"340.23","currency":"PLN","title":"przełew własny"}' -H 'Content-Type: application/json' http://localhost:4567/transfer

expected result:

{
  "status": "PENDING",
  "message": "Transfer successfully created",
  "data": {
    "id": "00097fb3-3ddf-4f25-bdb4-4302ec24c2a4"
    "timestamp": "Jan 8, 2019 12:19:32 AM",
    "status": "PENDING",
    "source": "PL61109010140000071219812874",
    "destination": "CZ6508000000192000145399",
    "amount": 340.23,
    "currency": "PLN",
    "title": "przełew własny"
  }
}

2) Get transfer details by ID

GET:  localhost:4567/transfer/id

curl -H 'Content-Type: application/json' http://localhost:4567/transfer/00097fb3-3ddf-4f25-bdb4-4302ec24c2a4

expected result:

{
  "status": "PENDING",
  "message": "Transfer successfully created",
  "data": {
    "id": "00097fb3-3ddf-4f25-bdb4-4302ec24c2a4"
    "timestamp": "Jan 8, 2019 12:19:32 AM",
    "status": "PENDING",
    "source": "PL61109010140000071219812874",
    "destination": "CZ6508000000192000145399",
    "amount": 340.23,
    "currency": "PLN",
    "title": "przełew własny"
  }
}

3) Get all transfer details

GET:  localhost:4567/transfer

To test, add more transfers, using POST API, examples are in resources/*.json


 curl -d '{"source":"US122000103040445550000000","destination":"DE0445999991232449999999","amount":"48600.99","currency":"USD","title":"一步一步，灰狐狸返回宿舍"}' -H 'Content-Type: application/json' http://localhost:4567/transfer

 curl -d '{"source":"RU099912012000000031399999999","destination":"BA0090909090339494494949","amount":"99912999.12","currency":"RUB","title":"своровал в оффшоры, хрен найдете"}' -H 'Content-Type: application/json' http://localhost:4567/transfer
 
 List all transfer details:
 
 curl -H 'Content-Type: application/json' http://localhost:4567/transfer
 
 expected result:
 
 [
   {
     "id": "00097fb3-3ddf-4f25-bdb4-4302ec24c2a4",
     "timestamp": "Jan 8, 2019 12:19:32 AM",
     "status": "PENDING",
     "source": "PL61109010140000071219812874",
     "destination": "CZ6508000000192000145399",
     "amount": 340.23,
     "currency": "PLN",
     "title": "przełew własny"
   },
   {
     "id": "0b19fb94-f796-4866-991c-a3d8e1bc2974",
     "timestamp": "Jan 8, 2019 12:26:48 AM",
     "status": "PENDING",
     "source": "US122000103040445550000000",
     "destination": "DE0445999991232449999999",
     "amount": 48600.99,
     "currency": "USD",
     "title": "一步一步，灰狐狸返回宿舍"
   },
   {
     "id": "e4fa29fc-fd87-4406-8783-997168b80ef3",
     "timestamp": "Jan 8, 2019 12:27:43 AM",
     "status": "PENDING",
     "source": "RU099912012000000031399999999",
     "destination": "BA0090909090339494494949",
     "amount": 99912999.12,
     "currency": "RUB",
     "title": "своровал в оффшоры, хрен найдете"
   }
 ]
 
 
 4) Get filtered transfer details
 
    supported query parameters:
    
    4a) limit=<n> - limit returned records (default = 100)
    
    curl -H 'Content-Type: application/json' http://localhost:4567/transfer?limit=1

    expected result: one result
    
    4b) sort=<field> - sort output by field value. 
        supported fields are only fields, presented in the Transfer json object, 
          ex: status, source, destination, amount, currency, title
          
    curl -H 'Content-Type: application/json' http://localhost:4567/transfer?limit=1&sort=currency
    
    expected result: record with currency PLN
    
    4c) order= asc/desc -  sort direction, ascending or descending. default value - asc
    
    curl -H 'Content-Type: application/json' http://localhost:4567/transfer?limit=1&sort=timestamp&order=desc
          
    expected result: last added record with highest timestamp
    
    5c) <field>=<value> - filter records by field. filter is a field name
    
    curl -H 'Content-Type: application/json' http://localhost:4567/transfer?currency=USD      
    
    expected: returned record with currency = USD
    
    curl -H 'Content-Type: application/json' http://localhost:4567/transfer?status=PENDING
    
    expected: returned records with status = PENDING
    
  5) Execute transfer:
  
  PUT:  localhost:4567/transfer/id
  
  By design, transfer object created in pending state.
  To make money transfer complete, execute query with PUT verb, and Id,
  returned by POST  

    
   curl -H 'Content-Type: application/json' -X PUT http://localhost:4567/transfer/a7363958-a269-4c5a-b477-37c89373e20c
   
   expected result:
   status COMPLETE means transfer finished successfully
   
   {
     "status": "COMPLETED",
     "message": "Transfer executed",
     "data": {
       "id": "a7363958-a269-4c5a-b477-37c89373e20c",
       "timestamp": "Jan 8, 2019 12:16:32 AM",
       "status": "COMPLETED",
       "source": "PL61109010140000071219812874",
       "destination": "CZ6508000000192000145399",
       "amount": 340.23,
       "currency": "PLN",
       "title": "przełew własny"
     }
   }
   
   in case of error (transfer is in wrong state): status of the request ERROR,
   status of the transfer itself not changed
   
   
   {
     "status": "ERROR",
     "message": "Transfer execution not possible",
     "data": {
       "id": "a7363958-a269-4c5a-b477-37c89373e20c",
       "timestamp": "Jan 8, 2019 12:16:32 AM",
       "status": "COMPLETED",
       "source": "PL61109010140000071219812874",
       "destination": "CZ6508000000192000145399",
       "amount": 340.23,
       "currency": "PLN",
       "title": "przełew własny"
     }
   }
   
   6) Cancel transfer:
   
   DELETE:  localhost:4567/transfer/id
   
   Transfer in pending state can be cancelled. 
   If transfer is in wrong state, ex. completed, the error status will be returned
   
   curl -H 'Content-Type: application/json' -X DELETE http://localhost:4567/transfer/e4fa29fc-fd87-4406-8783-997168b80ef3
   
   expected result: transfer status changed to CANCELLED:
   
   {
     "status": "CANCELLED",
     "message": "Transfer cancelled",
     "data": {
       "id": "e4fa29fc-fd87-4406-8783-997168b80ef3",
       "timestamp": "Jan 8, 2019 12:27:43 AM",
       "status": "CANCELLED",
       "source": "RU099912012000000031399999999",
       "destination": "BA0090909090339494494949",
       "amount": 99912999.12,
       "currency": "RUB",
       "title": "своровал в оффшоры, хрен найдете"
     }
   }
   
   if transfer was in wrong state (not PENDING), error is returned:
   Select id of transfer, which was already completed 
   
   curl -H 'Content-Type: application/json' -X DELETE http://localhost:4567/transfer/a7363958-a269-4c5a-b477-37c89373e20c
   
   expected result:

   {
     "status": "ERROR",
     "message": "Transfer cancellation not possible",
     "data": {
       "id": "a7363958-a269-4c5a-b477-37c89373e20c",
       "timestamp": "Jan 8, 2019 12:16:32 AM",
       "status": "COMPLETED",
       "source": "PL61109010140000071219812874",
       "destination": "CZ6508000000192000145399",
       "amount": 340.23,
       "currency": "PLN",
       "title": "przełew własny"
     }
   }
   

   
    
    
