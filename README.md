----------------EDUTRACK - Student Portal-------------------

EduTrack is a backend application developed using Java and Spring Boot for managing students, courses, batches, attendance and payments.

The project provides REST APIs and uses MongoDB for storing data. JWT authentication and role-based authorization are used to secure the APIs. 
Razorpay is also integrated for online fee payments.

   🛠️ TECHNOLOGIES USED
---------------------------------------------

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data MongoDB
- MongoDB
- Maven
- ModelMapper
- Bean Validation
- Razorpay API
- REST API
- Postman
- IntelliJ IDEA
- Git and GitHub


✨ FEATURES
---------------------------------------------

- Student management
- Course management
- Batch management
- User registration and login
- JWT based authentication
- Role based authorization for Admin and Student
- Attendance management
- Online fee payment using Razorpay
- Razorpay payment verification using signature
- Input validation
- Global exception handling
- MongoDB database
- REST APIs


🏗️ PROJECT STRUCTURE
---------------------------------------------

The project follows a layered architecture:

Controller → Service → Repository → MongoDB

Main packages:

- controller - Handles REST API requests
- service - Contains business logic
- repository - Handles database operations
- model - Contains MongoDB entities
- dto - Contains Data Transfer Objects
- security - JWT authentication and Spring Security configuration
- exception - Exception handling
- config - Application configurations


🔐 AUTHENTICATION AND AUTHERIZATION
---------------------------------------------
  
The application uses Spring Security and JWT for authentication.

After successful login, the user receives a JWT token. This token is sent with requests to protected APIs.

Example:
`Authorization: Bearer <JWT_TOKEN>`

The application has two roles:
- `ADMIN`
- `STUDENT`

Admin has access to management operations, while students have access to the APIs allowed for their role.
Passwords are stored using BCrypt password encryption.


👨‍🎓STUDENT MANAGEMENT
---------------------------------------------

The Student module is used to manage student information.

The main operations are:
- Add a new student
- Get all students
- Get a student by ID
- Search students
- Update student details
- Delete a student

Student data is stored in MongoDB using `StudentRepository`.

`StudentDTO` is used for transferring student data between the client and backend.

The Student APIs are protected using role-based authorization. 
Students can access the allowed GET operations, while administrative operations such as create, update and delete are restricted to the ADMIN role.


📚 COURSE AND BATCH MANAGEMENT
---------------------------------------------

### Course

The Course module is used to manage the courses available in the student portal.

The main operations are:
- Create a course
- Get course details
- Update a course
- Delete a course

Course information includes the course name, duration, fees and description.

### Batch

The Batch module is used to manage student batches.

Batch information includes:
- Batch name
- Course
- Trainer name
- Start date
- End date
- Capacity
- Status

The Batch module also uses validation for fields such as capacity and dates.

Course and Batch management operations are protected and can be performed by the ADMIN role.


📅 ATTENDANCE MANAGEMNET
---------------------------------------------

The Attendance module is used to record and view student attendance.

The main operations include:
- Mark attendance
- View attendance
- Update attendance
- Delete attendance

Attendance APIs are secured using role-based authorization.

- ADMIN can create, update and delete attendance.
- ADMIN and STUDENT can view attendance.

Attendance data is stored in MongoDB and is handled using the Controller, Service and Repository layers.


💳 RAZORPAY PAYMENT INTEGRATION
---------------------------------------------

Razorpay is integrated into the project for online student fee payments.

The payment process works in the following steps:

1. The user enters the Student ID and payment amount.
2. The backend creates a Razorpay order.
3. The Razorpay Order ID is saved in MongoDB with the payment details.
4. Razorpay Checkout is opened for the user.
5. After successful payment, Razorpay returns the Payment ID, Order ID and Signature.
6. These details are sent back to the Spring Boot backend.
7. The backend verifies the Razorpay signature using the Razorpay secret key.
8. If the signature is valid, the payment status is changed to `SUCCESS`.
9. Payment details such as Payment ID, Signature, Payment Method and Payment Date are saved in MongoDB.

The payment module contains:
- `PaymentController`
- `PaymentService`
- `RazorpayService`
- `PaymentRepository`
- `PaymentDTO`
- `Payment` model

Razorpay credentials are kept outside the source code using environment variables.


⚠️ VALIDATION AND EXCEPTION HANDLING
---------------------------------------------

Bean Validation is used to validate incoming request data.

Some validations used in the project are:
- `@NotBlank` for required fields
- `@Email` for email validation
- `@Positive` for positive values such as fees and payment amounts
- `@Min` for minimum values such as batch capacity
- `@Future` for validating future dates

The project also uses global exception handling to provide proper responses when an error occurs.

Examples include:
- Resource not found
- Invalid request data
- Duplicate resources
- Invalid credentials
- Invalid payment signature

This keeps error handling centralized instead of writing separate exception handling logic in every controller.


🔗 MAIN API ENDPOINTS
---------------------------------------------

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/users/login` | User login and JWT generation |

### Students

| Method | Endpoint | Description |
|---|---|---|
| POST | `/students` | Add student |
| GET | `/students` | Get all students |
| GET | `/students/{id}` | Get student by ID |
| PUT | `/students/{id}` | Update student |
| DELETE | `/students/{id}` | Delete student |

### Courses

| Method | Endpoint | Description |
|---|---|---|
| POST | `/courses` | Create course |
| GET | `/courses` | Get courses |
| PUT | `/courses/{id}` | Update course |
| DELETE | `/courses/{id}` | Delete course |

### Batches

| Method | Endpoint | Description |
|---|---|---|
| POST | `/batches` | Create batch |
| GET | `/batches` | Get batches |
| PUT | `/batches/{id}` | Update batch |
| DELETE | `/batches/{id}` | Delete batch |

### Attendance

| Method | Endpoint | Description |
|---|---|---|
| POST | `/attendance` | Mark attendance |
| GET | `/attendance` | View attendance |
| PUT | `/attendance/{id}` | Update attendance |
| DELETE | `/attendance/{id}` | Delete attendance |

### Payments

| Method | Endpoint | Description |
|---|---|---|
| POST | `/payments/create-order` | Create Razorpay order |
| POST | `/payments/verify` | Verify Razorpay payment |


🗄️DATABASE
---------------------------------------------

MongoDB is used as the database for this project.

The application uses Spring Data MongoDB and `MongoRepository` for database operations.

Main collections used in the project are:
- `students`
- `courses`
- `batches`
- `users`
- `attendance`
- `payments`

The database name used during development is:
`edutrack`

MongoDB stores the relationships between modules using IDs such as `studentId`, `courseId` and `batchId`.


## ▶️ How to Run
---------------------------------------------

1. Clone the repository
```bash
git clone https://github.com/aishwaryasatpute07/Edutrack---studentportal.git
```

2. Open the project
Open the cloned project in IntelliJ IDEA.

3. Start MongoDB
Make sure MongoDB is running on your system.
The project uses the `edutrack` database.

4. Configure Razorpay
Razorpay credentials are configured using environment variables.

```text
RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
```

5. Run the application
Run the Spring Boot application from IntelliJ IDEA or using Maven:

mvn spring-boot:run

The application runs on:
http://localhost:8080


🧪 TESTING
---------------------------------------------

The REST APIs were tested using Postman.

The main modules tested include:

- User login and JWT authentication
- Student APIs
- Course APIs
- Batch APIs
- Attendance APIs
- Payment order creation
- Razorpay payment verification
- Role-based authorization
- Validation and exception handling
