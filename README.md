# EduTrack Student Portal

EduTrack provides role-based admin, instructor, and student workspaces backed by Spring Boot and MongoDB. Students can review enrollment fees, pay the backend-calculated outstanding balance through Razorpay, view attendance and academic results, and download PDF reports, verified receipts, and examination hall tickets. Admins manage fee components, academic data, and hall-ticket issuance; assigned instructors can update attendance and marks for their batch students.

## Configuration

Set the following environment variables before starting the application:

* `JWT_SECRET` — required signing secret of at least 32 bytes. Changing it invalidates existing JWTs.
* `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` — required Razorpay credentials; payment verification fetches the gateway payment server-side.
* `RAZORPAY_WEBHOOK_SECRET` — optional webhook secret. Configure it in Razorpay to receive signed success/failure events.
* `MONGODB_URI` — optional MongoDB connection string; defaults to `mongodb://localhost:27017/edutrack`.

Payment order and payment IDs have unique MongoDB indexes. Check existing payment records for duplicates before deploying with auto-index creation enabled. Instructor access requires admins to set the assigned batch's trainer login username.

Run the application with `.\mvnw.cmd spring-boot:run`, then sign in at `/admin.html`. `payment.html` uses the signed-in student's session and cannot create orders without a linked student login.

## Current scope

Fee totals combine course tuition, direct child-course/module fees, enrollment sports/examination/other charges, and discounts. Academic pass status currently uses a 50% threshold. PDF documents use a built-in standard font, so unsupported non-Latin characters are rendered as placeholders. Existing CSV receipt, marksheet, and attendance report endpoints remain available alongside PDF downloads.

**Security caveat:** Passwords are currently stored and compared as plain text, as previously requested. They are omitted from API responses, but this storage approach is not safe for production credentials and should be replaced with password hashing before deployment.
