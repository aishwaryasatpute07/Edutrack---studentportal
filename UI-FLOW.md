# EduTrack UI Flows

This guide explains how to use the current portal from sign-in through the main admin and student tasks. The page is served at `/admin.html`; it chooses a workspace based on the role returned by the backend after login.

## Before starting

1. Start MongoDB and the Spring Boot application.
2. Configure `JWT_SECRET`, `RAZORPAY_KEY_ID`, and `RAZORPAY_KEY_SECRET` in the application's environment. Set `RAZORPAY_WEBHOOK_SECRET` if Razorpay webhooks are enabled.
3. Open `http://localhost:8080/admin.html`.
4. Sign in with an account already registered in the system. New accounts are created by an administrator through the Accounts section; there is no public self-registration flow.

The login form sends the username and password to `POST /users/login`. On success, the browser stores the returned JWT and role in session storage. Student accounts also need a linked student profile ID. Sign out clears the session values. The browser sends the JWT with protected API requests; backend authorization still controls access.

## Administrator flow

### 1. Sign in and open the administration workspace

Sign in using an account whose role is `ADMIN`. The UI loads the administrator workspace and fetches the course hierarchy, students, batches, attendance, enrollments, payments, user accounts, academic records, and hall tickets from the backend.

The navigation tabs open Overview, Courses, Students, Accounts, Batches, Attendance, Enrollments, Payments, Reports, and Academic records. Use **Refresh** on a section to reload its server records.

### 2. Create the student and login account

1. Open **Students**, choose **Add student**, enter the student's profile fields, optionally select a batch, and save.
2. Open **Accounts**, choose **Add account**, enter a unique username, email, and password, choose the `STUDENT` role, then link the account to the student profile just created.
3. Save the account. The API does not return passwords in account listings.

An instructor account is created similarly, with role `INSTRUCTOR`. Instructors need no student-profile link.

### 3. Set up courses and batches

1. Open **Courses** and create top-level courses and, if needed, child courses/modules. Course fees and child-module fees contribute to the fee summary.
2. Open **Batches** and create a batch for a course. Enter its dates, capacity, trainer display name, and the instructor account's exact login username in **Trainer login username**.
3. Save the batch, then assign students to it in the Students section. Instructor data access is limited to students in batches assigned to that instructor.

### 4. Enroll a student and configure fees

1. Open **Enrollments** and choose **Enroll student**.
2. Select the student and course and save.
3. In the enrollment row, choose **Edit fees** to set sports/extracurricular charges, examination fees, other charges, and any discount.

The fee summary is calculated from course tuition, direct child-course/module fees, enrollment charges, and discount. Paid and outstanding totals are based on verified payments. A cancelled enrollment cannot start a new fee payment or receive a hall ticket.

### 5. Record attendance and academic results

1. In **Attendance**, choose **Record attendance**, select a student, date, and status (`PRESENT`, `ABSENT`, `LATE`, or `EXCUSED`), then save.
2. In **Academic records**, choose **Add result**, select the student, course, subject/module, examination, attempt number, marks, and maximum marks, then save.
3. Open **Reports** and select a course to view enrollment and performance summaries.

The backend validates enrollment, course/subject relationships, marks, duplicate attendance dates, duplicate result attempts, and the caller's role. The student workspace is read-only for these academic records.

### 6. Issue examination hall tickets

1. Open the **Hall tickets** area within **Academic records** and choose **Issue hall ticket**.
2. Select an actively enrolled student and course, enter examination name, exam date, start time, venue, seat number, and any instructions.
3. Save the ticket. Edit or remove it from the hall-ticket table when appropriate.

Seat numbers cannot be duplicated for the same course, examination, and date. Only administrators can issue, edit, or delete tickets.

### 7. Review payment activity

Open **Payments** to view fee balances and the payment ledger. Only gateway-verified payments count as collected. Use the receipt action on a verified payment to download its PDF receipt. The payment ledger and balances refresh periodically while the administrator workspace is open.

## Student flow

### 1. Sign in

Open `/admin.html` and sign in with the `STUDENT` account linked to the student's profile. The UI switches to the student dashboard and loads that profile's own data: profile, enrolled courses, fee summaries, attendance, academic records, performance, payment history, and hall tickets.

If the account is not linked to an existing student profile, an administrator must fix the account link before student features can load. Student API access is checked against the authenticated account on the backend; changing a student ID in the browser does not grant access to another student's records.

### 2. Review profile and course information

The dashboard shows the latest student profile, course, batch, trainer/instructor details, and last-updated time where available. Course rows show enrollment status and available progress, attendance, and marks data. The student cannot edit these records.

### 3. Review attendance and download a report

The attendance panel lists daily records and shows the attendance distribution, working days, days present, days absent, and percentage. Choose **Download report** for a PDF attendance report. Staff changes appear after refresh or the dashboard's periodic reload.

### 4. Review marks and download a marksheet

The academic records panel lists course, subject, examination, attempt, marks, pass/backlog status, and update date. Choose **Download marksheet** for a PDF. Student access is scoped to that student's records.

### 5. Review fees and pay

1. Open the course fee row and expand **Fee breakdown** to review tuition, subject/module fees, sports, examination, other charges, discount, total, paid amount, and outstanding balance.
2. Choose **Pay outstanding balance** for the course with a remaining balance.
3. The browser requests a payment order using the course ID only. The server derives the student identity from the authenticated account and calculates the exact outstanding amount.
4. Complete the Razorpay checkout. On callback, the browser sends the order, payment, and signature to the backend.
5. The backend verifies the signature and fetches the transaction from Razorpay before marking the payment verified. The fee balance and payment history refresh after the verification attempt.

Do not treat the checkout success screen by itself as proof of payment. A receipt is available only after the backend marks the payment `VERIFIED`. The separate `/payment.html` page uses the same signed-in student session and fee/payment APIs.

### 6. View payment history and receipts

The payment history panel shows course, amount, status, and transaction date. Download the PDF receipt for verified transactions. Pending or failed attempts do not reduce the amount due.

### 7. Download an examination hall ticket

The hall-ticket panel lists tickets issued for this student. Choose **Download PDF** to download a ticket containing the examination, date/time, venue, assigned seat, and instructions. The endpoint checks ticket ownership against the authenticated student.

## Instructor flow

1. Sign in using an `INSTRUCTOR` account.
2. The dashboard loads students from batches whose trainer username matches that account's login username.
3. Choose a student to load their attendance and academic results.
4. Use **Record attendance** or **Add result** to create records, or the row's **Edit** action to update an existing record.

The backend verifies the instructor is assigned to the student's batch before allowing academic reads or writes. Instructors cannot manage fees, accounts, enrollments, or hall tickets.

## Troubleshooting

* **Login works but the dashboard is empty:** verify that the login account has the correct role and, for students, a valid linked student profile.
* **Instructor has no assigned students:** verify the student's batch and that the batch's trainer login username exactly matches the instructor's account username.
* **Payment order fails:** confirm Razorpay key configuration, that the student is enrolled in the course, and that the enrollment is not cancelled or fully paid.
* **Payment is not yet verified:** check the payment history and server-side gateway configuration. The UI does not mark a payment successful solely from a browser callback.
* **Ticket or document is unavailable:** verify the data exists and that the signed-in account is authorized for that student. Hall tickets must be issued by an admin for an active enrollment.
* **A login token stops working after secret rotation:** sign in again using a token issued with the current `JWT_SECRET`.

## Current behavior and limitations

* Fee calculation treats direct child courses/modules as fee-bearing subjects.
* Academic pass/fail currently uses a fixed 50% threshold.
* PDF generation uses a standard built-in font; some non-Latin characters may display as placeholders. CSV endpoints remain available for receipts, marksheets, and attendance reports.
* Passwords are currently stored and compared as plain text, following the earlier project request. They are excluded from API responses, but plain-text password storage is not suitable for production.
