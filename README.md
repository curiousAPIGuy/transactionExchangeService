

TransactionExchangeService

This is java 17 spring boot application that tries to 
Requirement #1: Store a Purchase Transaction
Requirement #2: Retrieve a Purchase Transaction in a Specified Country’s Currency

Implementation Details

This application provides an API for 
    POST /api/transactions (store a purchase Transaction)  
    GET  /api/transactions/{id}&currency=? ( retrieve in a specified currency)

It uses external "TreasuryAPIClient" to fetch the latest exchangeRate within the last 6 months and returns the desired response.

The application uses H2 Database for development and can be configured for PostgreSQL in production.






