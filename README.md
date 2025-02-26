# **1. payment service for PAYVERDE**

`A simple Java web application simulating money payment operations between users and merchants. The application includes REST APIs for processing transactions, retrieving transaction details.`

---

## **Features**
- REST APIs for payment and transaction management.
- Support for optional transaction filters (merchantId, userId, date range).

---

# **2. Technology Stack**
- **Framework**: Spring Boot  
- **Language**: Java 11+  
- **Database**: MySQL  
- **Build Tool**: Maven   

---

## **3. Prerequisites**
1. **Java Development Kit (JDK)**: Version 11 or higher.  
2. **Maven**: Version 3.6 or higher.  
3. **Docker (optional)**: For containerized deployment.  
4. **Kubernetes (optional)**: For orchestrating multiple instances.  

---

# **4. Installation Instructions**
### **Clone the Repository**
```bash
git clone https://github.com/geminidolapo/payment-service.git
```
#### Clean up the application  
```bash
cd payment-service
``` 
```bash
mvn clean install
```
#### To run the application 
```bash
mvn spring-boot:run
```
---

# **5. API Documentation**
## **API Endpoints**

### 1. User Registration
- **Endpoint**: `POST /api/v1/auth/register-user`
- **Description**: To register a user.
- **Request Body**:
```
    {
        "username": "JohnDoe",
        "password": "SecurePass1",
        "firstname": "John",
        "lastname": "Doe",
        "email": "johndoe@example.com",
        "roles": ["USER"]
    }
```

### 2. Merchant Registration
- **Endpoint**: `POST /api/v1/auth/register-merchant`
- **Description**: To register a merchant.
- **Request Body**:
```
    {
        "username": "JohnDoe",
        "password": "SecurePass1",
        "firstname": "John",
        "lastname": "Doe",
        "email": "johndoe@example.com",
        "roles": ["MERCHANT"]
    }
```

### 3. User Login
- **Endpoint**: `POST /api/v1/auth/user-login`
- **Description**: To generate access token for user.
- **Request Body**:
```
    {
        "username": "JohnDoe",
        "password": "SecurePass1"
    }
```

### 4. Merchant Login
- **Endpoint**: `POST /api/v1/auth/merchant-login`
- **Description**: To generate access token for merchant.
- **Request Body**:
```
    {
        "username": "JohnDoe",
        "password": "SecurePass1"
    }
```

### 5. Generate Qr Code
- **Endpoint**: `POST /api/v1/payment/generate-qrcode`
- **Description**: To generate qr code for payment.
- **Request Body**:
```
    {
         "amount": 50.0,
         "currency": "NGN",
         "description": "Purchase of electronics",
         "merchantId": "1"
    }
```

### 6. Retrieve User Payments
- **Endpoint**: `GET /api/v1/payment/user-payment`
- **Description**: retrieves a list of user's transaction.
- **Query Parameters**:
    merchantId (optional): Filter by merchant id.
    startDate and endDate (optional): Date range for transactions.

### 7. Retrieve User Payments
- **Endpoint**: `GET /api/v1/payment/merchant-payment`
- **Description**: retrieves a list of merchant's transaction.
- **Query Parameters**:
  userId (optional): Filter by user id.
  startDate and endDate (optional): Date range for transactions.

### 8. Process Payment
- **Endpoint**: `POST /api/v1/payment/process`
- **Description**: To process for payment.
- **Request Body**:
```
    {
         "encryptedData":"csVxtL/aphm2tSo+oS58c1wIWxoEuSdsZdDADRxw8oLteVeW1+gbICMvnP2kza8vPU9CfgzgriqiV9wD/lVvoA=="
    }
```
### 9. Fund User Account
- **Endpoint**: `POST /api/v1/payment/fund`
- **Description**: To fund for user wallet.
- **Request Body**:
```
    {
         "userId": "1",
         "amount": 10.00,
         "currency": "NGN"
    }
```
---
# **6. Running Tests**
#### 1. Unit Tests
Run unit tests with Maven:
```bash
mvn test
```

#### 2. Integration Tests
https://documenter.getpostman.com/view/26196556/2sAYdfpAVZ


## **7. Troubleshooting**
#### Troubleshooting

##### Database Connection Issues
- Double-check the credentials in `application-dev.properties`.

##### Port Conflict
- If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```