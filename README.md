

TransactionExchangeService

This is java 17 spring boot application that tries to 
Requirement #1: Store a Purchase Transaction
Requirement #2: Retrieve a Purchase Transaction in a Specified Country’s Currency

Architecture

The application has context path /wex/v1

The application follows a layered architecture.

Controller Layer: Handles HTTP requests and responses <PurchaseTransactionController>

Service Layer: Contains the business logic. <PurchaseTransactionService>

Repository Layer: Handles data access. <PurchaseTransactionRepository> This is using H2 in-file database for development and can be configured for PostgreSQL in production.

Client Layer: This is using TreasuryAPIClient to fetch the latest exchangeRate within the last 6 months and returns the desired response. <TreasuryApiClient>

API Endpoints

This application provides an API for 
    POST /api/transactions (store a purchase Transaction)  
    GET  /api/transactions/{id}&currency=? ( retrieve in a specified currency)


How to Run and Test
Start the application:


curl -X POST http://localhost:8080/wex/v1/api/transactions \
     -H "Content-Type: application/json" \
     -d '{
           "description": "Flight to Europe",
           "transactionDate": "2023-10-15T10:00:00",
           "amount": 1000.00
         }'


Get a converted transaction:


curl "http://localhost:8080/api/transactions/{id}?currency=United Kingdom-Pound"


Updates:

Per comments from Tony:
Updated the Entity to include DateTime
The external treasuryAPI uses Date as purchase date, so the service fetches the exchange rate for the purchase date.

Updated the production configuration to use PostgreSQL instead of H2 Database
Updated the pom.xml file to include PostgreSQL dependency
We can use docker-compose file to spin up the PostgreSQL and Adminer   






