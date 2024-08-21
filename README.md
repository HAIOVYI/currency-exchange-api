# Currency Exchange Service API

Currency Exchange Service is a RESTful API that facilitates the buying and selling of currencies. The API is divided into two parts: an admin section and a user section, designed for two distinct roles—administrator and user. All endpoints require authenticated access, with authorization managed via JWT tokens.

## Project Description

The Currency Exchange Service API includes the following domain models:
- **User**: Represents registered users, including authentication, personal details, and role-based access control.
- **Role**: Defines user roles such as ADMIN and USER, and manages access permissions.
- **Currency**: Represents the available currencies in the system, including their codes, names, and balances.
- **CurrencyRate**: Tracks exchange rates for different currencies, along with timestamps to support historical rate queries.
- **Exchange**: Represents a currency exchange request made by a user, including the currencies involved, amount, exchange rate, and request status (PENDING, COMPLETED, CANCELLED).

## Technologies Used

- **Java**: Core programming language.

- **Spring Boot**: For rapid development and deployment of a production-grade application.

- **Spring Security**: For authentication and authorization.

- **Spring Data JPA**: For simplified data access and management.

- **Swagger**: For API documentation.

- **MapStruct**: For type-safe data mapping between entities and DTOs.

- **Liquibase**: For database version control and schema migration.

- **JWT (JSON Web Tokens)**: For secure token-based authentication.

- **MySQL**: For relational data storage.

- **WebSocket**: For real-time notifications.

- **Docker**: For containerization and consistent deployment.

- **JUnit & MockMvc**: For integration testing.

## Functionalities

### Admin Functionalities
- **User Management**: Register and block users.
- **Currency Management**: Add and remove currencies.
- **Rate Management**: Update currency rates.

### User Functionalities
- **Authentication**: Register and log in.
- **View Rates**: Check current and historical currency exchange rates, including maximum and minimum values over a selected period.
- **Currency Exchange**: Submit a currency exchange request at the current rate. Requests are either fulfilled automatically or held for future execution, depending on available resources. Users are notified via WebSocket when the request can be processed, and they can accept or reject the updated offer.

## Setup Instructions

### Prerequisites
- **Docker**
- **Docker Compose**

### Installation Steps

1. **Clone the repository:**
    ```bash
    git clone git@github.com:HAIOVYI/currency-exchange-api.git
    cd currency-exchange-api
    ```


2. **Run the application using Docker Compose:**
    ```bash
    docker-compose up --build
    ```

### Testing

- **Run tests:**
    ```bash
    mvn test
    ```

## API Documentation

You can access the full API documentation at:

### [Swagger UI](http://localhost:8080/api/swagger-ui/index.html#/)

## 🔌 Endpoints

## Admin Operations
Use the following credentials to login as an admin:

- Email: **admin@gmail.com**
- Password: **adminadmin**

### User Endpoints

- **Register a new user**: `POST /api/register`
    ```json
    {
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "password": "securePassword123",
      "confirmPassword": "securePassword123"
    }
    ```

- **Authenticate a user**: `POST /api/login`
    ```json
    {
      "email": "john.doe@example.com",
      "password": "securePassword123"
    }
    ```

### Currency Endpoints

- **Get currency by ID**: `GET /currency/{id}`
- **Get all currencies**: `GET /currency`
- **Create a new currency**: `POST /currency` *(Requires ADMIN role)*
```json
    {
      "code": "CAD",
      "name": "Canadian Dollars"
    }
```
- **Delete currency by ID**: `DELETE /currency/{id}` *(Requires ADMIN role)*

### Currency Rate Endpoints

- **Get current exchange rate**: `GET /currency-rate/current`
    - Query parameters: `fromCurrency`, `toCurrency`
- **Get historical exchange rates**: `GET /currency-rate/history`
    - Query parameters: `fromCurrency`, `toCurrency`, `fromDate`, `toDate`
- **Get base currency rates**: `GET /currency-rate/base`
    - Query parameter: `baseCurrency`
- **Create a new currency rate**: `POST /currency-rate` *(Requires ADMIN role)*
```json
    {
      "currencyId": "2",
      "rate": "11.0034"
    }
```

### Currency Exchange Endpoints

- **Create a new exchange request**: `POST /exchange`
```json
    {
      "currencyFromId": "2",
      "currencyToId": "1",
      "amount": "100.00"
    }
```

### User Management Endpoints

- **Block a user**: `PUT /user/{userId}/block` *(Requires ADMIN role)*

# Currency Exchange Service - WebSocket Integration

This document provides instructions on how to use the WebSocket integration for real-time notifications regarding the status of currency exchange requests in the Currency Exchange Service.

## WebSocket Endpoint

- **Endpoint URL**: `ws://localhost:8080/api/websocket`

## How to Use

### 1. Connect to the WebSocket

To connect to the WebSocket endpoint, you can use a WebSocket client such as Postman or a custom WebSocket client in your application.

**Using Postman**:
1. Open Postman.
2. Go to the "WebSocket" tab.
3. Enter the WebSocket URL: `ws://localhost:8080/api/websocket`
4. Click "Connect".

### 2. Subscribe to Notifications

Once connected, you will receive real-time notifications about the status of your currency exchange requests. These notifications will include updates when an exchange request can be processed or if there are any changes in its status.

### 3. Handle Notifications

The server will send messages in the following format:

```json
{
  "id": 123,
  "userId": 1,
  "currencyFrom": 2,
  "currencyTo": 1,
  "amount": 100.00,
  "rate": 0.85,
  "timestamp": "2024-08-21T14:27:22.65777",
  "status": "PENDING"
}
```

- **`id`**: The unique identifier for the exchange request.
- **`userId`**: The ID of the user who created the exchange request.
- **`currencyFrom`**: The ID of the currency being exchanged.
- **`currencyTo`**: The ID of the currency being received.
- **`amount`**: The amount of currency being exchanged.
- **`rate`**: The exchange rate at the time of the request.
- **`timestamp`**: The time when the notification was generated.
- **`status`**: The current status of the exchange request (e.g., `PENDING`).



3. **Confirm or Reject Requests**: If you receive a notification like this stared above, it means the server has received the money for this exchange. You should check if the rate is acceptable for you:

    ```json
    {
      "exchangeId": 123,
      "confirmed": true
    }
    ```

- **`exchangeId`**: The unique identifier for the exchange request.
- **`confirmed`**: Set to `true` to confirm the request or `false` to reject it.


  Add the following header to your WebSocket message:
- **Authorization**: `Bearer {{jwt}}`
  Replace `{{jwt}}` with your actual JWT token.

## Contact
[![Linkedin](https://i.sstatic.net/gVE0j.png) LinkedIn](https://www.linkedin.com/in/oleksii-haiovyi-28967b303/)
&nbsp;
[![GitHub](https://i.sstatic.net/tskMh.png) GitHub](https://github.com/HAIOVYI)
