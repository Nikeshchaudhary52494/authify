# **Authify — Spring Boot Authentication Service**

Authify is a **Spring Boot** based authentication service that provides secure user registration, login, profile management, and logout functionality.
It supports **OTP-based email verification**, **password reset via OTP**, and uses **JWT authentication** with **HTTP-only cookies** for secure session management.

---

## 🚀 **Features**

### **User Registration**

- Register with email and password
- OTP sent to email for account verification
- DTO-based request validation

### **User Login**

- Login with verified email and password
- JWT token issued and stored in HTTP-only cookie

### **User Profile View**

- Retrieve profile information for authenticated user

### **User Logout**

- Clear authentication cookie

### **Account Verification**

- OTP sent to registered email
- Verify account before login

### **Password Reset**

- OTP sent to email for password change
- Secure password update after OTP verification

### **Validation with DTO**

- Request payloads validated with **Spring Validation** (`@Valid`, `@NotNull`, `@Email`, etc.)

### **Centralized Exception Handling**

- Custom global exception handler using `@RestControllerAdvice`

### **JWT Authentication with Cookies**

- Stateless authentication
- HTTP-only cookie for JWT token to prevent XSS attacks

---

## 🛠 **Tech Stack**

- **Java 21+**
- **Spring Boot 3.x**
- **Spring Security**
- **Spring Data JPA**
- **Spring Validation**
- **Spring Email** (for sending OTP emails)
- **JWT (JSON Web Token)**
- **MySQL**
- **Maven** (build tool)

---

## ⚙️ **Configuration**

### **3️⃣ Configure Database and Email**

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/authify
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your_email@example.com
spring.mail.password=your_email_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

jwt.secret=your_jwt_secret
```

---

## ▶ **Build & Run**

```bash
mvnw spring-boot:run
```

---

## 📡 **API Endpoints**

| Method | Endpoint            | Description                                                      |
| ------ | ------------------- | ---------------------------------------------------------------- |
| POST   | `/register`         | Register a new user and get an initial profile response.         |
| POST   | `/send-otp`         | Send a verification OTP to the logged-in user's email.           |
| POST   | `/verify-otp`       | Verify a user's account using the OTP.                           |
| POST   | `/login`            | Authenticate a user and set a JWT cookie for session management. |
| GET    | `/is-authenticated` | Check if the current user is authenticated.                      |
| POST   | `/logout`           | Logout the user by clearing the JWT cookie.                      |
| POST   | `/password/request` | Request an OTP for a password reset via email.                   |
| POST   | `/password/reset`   | Reset a user's password using the provided email and OTP.        |
| GET    | `/profile`          | Get the profile details of the logged-in user.                   |

---

## 🔐 **Security**

- All protected endpoints require JWT authentication via **HTTP-only cookie**.
- OTP verification required before login.
- CSRF protection disabled for stateless API.
- DTO validation ensures clean and secure request data.

---

## 🛡 **Exception Handling**

- All exceptions handled centrally with `@RestControllerAdvice`
- Returns consistent JSON error responses

---

## 📧 **OTP Flow**

1. User registers → OTP sent to email
2. User verifies OTP → account activated
3. For password reset → OTP sent to email
4. OTP verified → password updated

---

## 🐳 **Run MySQL with Docker**

Spin up a **MySQL** container with a database named `authify`:

```bash
docker run -d \
  --name authify-mysql \
  -e MYSQL_ROOT_PASSWORD=secretpassword \
  -e MYSQL_DATABASE=authify \
  -p 3306:3306 \
  mysql:8.0
```

**Details:**

- `--name authify-mysql` → container name
- `-e MYSQL_ROOT_PASSWORD=secretpassword` → root password (change in production)
- `-e MYSQL_DATABASE=authify` → creates database `authify` automatically
- `-p 3306:3306` → exposes MySQL on host port `3306`
- `mysql:8.0` → MySQL version

---

## 🔗 **Connection URL**

```
mysql://root:secretpassword@localhost:3306/authify
```
