package com.edutrack.studentportal.service;

import com.edutrack.studentportal.config.RazorpayConfig;
import com.edutrack.studentportal.dto.CreatePaymentOrderDTO;
import com.edutrack.studentportal.dto.VerifyPaymentDTO;
import com.edutrack.studentportal.dto.FeeSummaryDTO;
import com.edutrack.studentportal.dto.PaymentDTO;
import com.edutrack.studentportal.dto.PaymentLedgerDTO;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.exception.PaymentGatewayException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.model.Payment;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.repository.PaymentRepository;
import com.razorpay.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.razorpay.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;
    private final RazorpayService razorpayService;
    private final RazorpayConfig.RazorpayProperties razorpayProperties;
    private final CourseAuditService courseAuditService;
    private final StudentAccessService studentAccessService;

    public PaymentService(PaymentRepository paymentRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          ModelMapper modelMapper,
                          RazorpayService razorpayService,
                          RazorpayConfig.RazorpayProperties razorpayProperties,
                          CourseAuditService courseAuditService,
                          StudentAccessService studentAccessService) {

        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.modelMapper = modelMapper;
        this.razorpayService = razorpayService;
        this.razorpayProperties = razorpayProperties;
        this.courseAuditService = courseAuditService;
        this.studentAccessService = studentAccessService;
    }

    public List<PaymentLedgerDTO> getPaymentLedger() {
        studentAccessService.requireAdministrator();
        return paymentRepository.findAll().stream().map(payment -> {
            PaymentLedgerDTO ledger = new PaymentLedgerDTO();
            ledger.setId(payment.getId());
            ledger.setStudentId(payment.getStudentId());
            ledger.setCourseId(payment.getCourseId());
            ledger.setAmount(payment.getAmount());
            ledger.setOrderId(payment.getOrderId());
            ledger.setPaymentId(payment.getPaymentId());
            ledger.setPaymentMethod(payment.getPaymentMethod());
            ledger.setStatus(payment.getStatus());
            ledger.setPaymentDate(payment.getPaymentDate());
            ledger.setCreatedAt(payment.getCreatedAt());
            return ledger;
        }).toList();
    }

    public PaymentDTO createPaymentOrder(CreatePaymentOrderDTO request) {
        if (request.getCourseId() == null || request.getCourseId().isBlank()) {
            throw new InvalidRequestException("courseId is required");
        }

        String studentId = studentAccessService.requireCurrentStudentId();
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, course.getId())
                .orElseThrow(() -> new InvalidRequestException("Student is not enrolled in this course"));

        if ("CANCELLED".equalsIgnoreCase(enrollment.getStatus())) {
            throw new InvalidRequestException("This enrollment is cancelled and cannot pay fees");
        }

        FeeSummaryDTO feeSummary = createStudentFeeSummary(studentId, course, enrollment);
        long amountInPaise = toPaise(feeSummary.getPendingAmount());
        if (amountInPaise <= 0) {
            throw new InvalidRequestException("No outstanding balance is due for this course");
        }

        Payment payment = new Payment();
        for (Payment existing : paymentRepository.findAllByStudentIdAndCourseId(studentId, course.getId())) {
            if ("CREATED".equalsIgnoreCase(existing.getStatus())
                    || "PROCESSING".equalsIgnoreCase(existing.getStatus())) {
                if (toPaise(existing.getAmount()) == amountInPaise) {
                    return toPaymentDTO(existing);
                }
                existing.setStatus("CANCELLED");
                existing.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(existing);
            } else if ("CREATING".equalsIgnoreCase(existing.getStatus())) {
                if (existing.getCreatedAt() == null
                        || existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
                    throw new InvalidRequestException("A payment order is already being prepared; retry shortly");
                }
                existing.setStatus("FAILED");
                existing.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(existing);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        payment.setStudentId(studentId);
        payment.setCourseId(course.getId());
        payment.setAmount(fromPaise(amountInPaise));
        payment.setTotalFeeAtOrder(feeSummary.getTotalFee());
        payment.setFeeBreakdown(toFeeBreakdown(feeSummary));
        payment.setStatus("CREATING");
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        paymentRepository.save(payment);

        Order order;
        try {
            order = razorpayService.createOrder(amountInPaise);
        } catch (Exception exception) {
            payment.setStatus("FAILED");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new PaymentGatewayException("The payment provider could not create an order", exception);
        }
        payment.setOrderId(order.get("id"));
        payment.setStatus("CREATED");
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        courseAuditService.record("FEE_ORDER_CREATED", course.getId(), course.getCourseName(),
                "Fee order created for student " + studentId + ": " + savedPayment.getOrderId());
        return toPaymentDTO(savedPayment);
    }

    public PaymentDTO verifyPayment(VerifyPaymentDTO request) {
        String studentId = studentAccessService.requireCurrentStudentId();
        Payment payment = paymentRepository.findByOrderId(request.getOrderId());

        if (payment == null) {
            throw new ResourceNotFoundException("Payment order not found: " + request.getOrderId());
        }
        if (!studentId.equals(payment.getStudentId())) {
            throw new org.springframework.security.access.AccessDeniedException("This payment order belongs to another student");
        }

        if ("VERIFIED".equalsIgnoreCase(payment.getStatus())) {
            if (request.getPaymentId().equals(payment.getPaymentId())) {
                return toPaymentDTO(payment);
            }
            throw new InvalidRequestException("This order has already been verified");
        }
        if (!List.of("CREATED", "FAILED", "CANCELLED").contains(
                payment.getStatus() == null ? "" : payment.getStatus().toUpperCase())) {
            throw new InvalidRequestException("This payment order is not available for verification");
        }
        if (paymentRepository.findByPaymentId(request.getPaymentId()) != null) {
            throw new InvalidRequestException("This gateway payment has already been processed");
        }

        boolean validSignature;
        try {
            validSignature = Utils.verifySignature(
                    request.getOrderId() + "|" + request.getPaymentId(),
                    request.getSignature(),
                    razorpayProperties.getKeySecret());
        } catch (Exception exception) {
            throw new InvalidRequestException("Invalid payment signature");
        }
        if (!validSignature) {
            throw new InvalidRequestException("Invalid payment signature");
        }

        com.razorpay.Payment gatewayPayment;
        try {
            gatewayPayment = razorpayService.fetchPayment(request.getPaymentId());
        } catch (Exception exception) {
            throw new PaymentGatewayException("Payment verification is temporarily unavailable", exception);
        }

        String gatewayOrderId = gatewayPayment.get("order_id");
        String gatewayStatus = gatewayPayment.get("status");
        Number gatewayAmount = gatewayPayment.get("amount");
        if (!request.getOrderId().equals(gatewayOrderId)
                || gatewayAmount == null || gatewayAmount.longValue() != toPaise(payment.getAmount())) {
            throw new InvalidRequestException("Gateway payment details do not match this order");
        }
        if (!"captured".equalsIgnoreCase(gatewayStatus)) {
            throw new InvalidRequestException("The gateway has not captured this payment");
        }

        Course course = courseRepository.findById(payment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + payment.getCourseId()));
        payment.setPaymentId(request.getPaymentId());
        payment.setSignature(null);
        payment.setPaymentMethod(gatewayPayment.get("method"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("VERIFIED");
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        courseAuditService.record("PAYMENT_VERIFIED", course.getId(), course.getCourseName(),
                "Payment verified for student " + studentId);
        return toPaymentDTO(updatedPayment);
    }

    public FeeSummaryDTO getCourseFeeSummary(String courseId) {
        studentAccessService.requireAdministrator();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        double totalPaid = 0;
        double totalDue = 0;
        for (Enrollment enrollment : enrollments) {
            FeeSummaryDTO summary = createStudentFeeSummary(enrollment.getStudentId(), course, enrollment);
            totalPaid += summary.getPaidAmount();
            totalDue += summary.getTotalFee();
        }

        FeeSummaryDTO summary = new FeeSummaryDTO();
        summary.setCourseId(course.getId());
        summary.setCourseName(course.getCourseName());
        summary.setCourseFee(course.getFees());
        summary.setTuitionFee(course.getFees());
        summary.setTotalFee(totalDue);
        summary.setPaidAmount(totalPaid);
        summary.setPendingAmount(Math.max(0, totalDue - totalPaid));
        summary.setPaymentStatus(totalPaid <= 0 ? "PENDING" : totalPaid >= totalDue ? "PAID" : "PARTIAL");
        return summary;
    }

    public List<FeeSummaryDTO> getStudentFeeSummary(String studentId) {
        studentAccessService.requireStudentOrAdminAccess(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<FeeSummaryDTO> summaries = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Course course = courseRepository.findById(enrollment.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + enrollment.getCourseId()));
            summaries.add(createStudentFeeSummary(studentId, course, enrollment));
        }
        return summaries;
    }

    public List<PaymentLedgerDTO> getStudentPaymentHistory(String studentId) {
        studentAccessService.requireStudentOrAdminAccess(studentId);
        return paymentRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toLedgerDTO)
                .toList();
    }

    public PaymentLedgerDTO getReceipt(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        studentAccessService.requireStudentOrAdminAccess(payment.getStudentId());
        if (!"VERIFIED".equalsIgnoreCase(payment.getStatus())) {
            throw new InvalidRequestException("A receipt is available only for verified payments");
        }
        return toLedgerDTO(payment);
    }

    public void processWebhook(String rawBody, String signature) {
        String webhookSecret = razorpayProperties.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new PaymentGatewayException("Payment webhooks are not configured", null);
        }
        if (signature == null || signature.isBlank()
                || !isValidWebhookSignature(rawBody, signature, webhookSecret)) {
            throw new InvalidRequestException("Invalid payment webhook signature");
        }

        JSONObject body;
        try {
            body = new JSONObject(rawBody);
        } catch (org.json.JSONException exception) {
            throw new InvalidRequestException("Invalid payment webhook payload");
        }
        String event = body.optString("event", "");
        JSONObject payload = body.optJSONObject("payload");
        JSONObject paymentObject = payload == null ? null : payload.optJSONObject("payment");
        JSONObject paymentEntity = paymentObject == null ? null : paymentObject.optJSONObject("entity");
        if (paymentEntity == null) {
            throw new InvalidRequestException("Webhook payload does not contain a payment");
        }

        String orderId = paymentEntity.optString("order_id", "");
        String paymentId = paymentEntity.optString("id", "");
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment order not found: " + orderId);
        }
        if (paymentEntity.optLong("amount", -1) != toPaise(payment.getAmount())) {
            throw new InvalidRequestException("Webhook amount does not match the payment order");
        }
        if ("VERIFIED".equalsIgnoreCase(payment.getStatus())) {
            if (paymentId.equals(payment.getPaymentId())) {
                return;
            }
            throw new InvalidRequestException("A different payment has already been verified for this order");
        }

        if ("payment.captured".equals(event)) {
            Payment existingPayment = paymentRepository.findByPaymentId(paymentId);
            if (existingPayment != null && !orderId.equals(existingPayment.getOrderId())) {
                throw new InvalidRequestException("Gateway payment has already been processed");
            }
            payment.setPaymentId(paymentId);
            payment.setPaymentMethod(paymentEntity.optString("method", null));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("VERIFIED");
        } else if ("payment.failed".equals(event)) {
            payment.setPaymentId(paymentId.isBlank() ? null : paymentId);
            payment.setStatus("FAILED");
        } else {
            return;
        }
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private boolean isValidWebhookSignature(String body, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)))
                    .getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(expected, signature.getBytes(StandardCharsets.US_ASCII));
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Could not validate payment webhook signature", exception);
        }
    }

    private FeeSummaryDTO createStudentFeeSummary(String studentId, Course course, Enrollment enrollment) {
        Map<String, Double> subjectFees = new LinkedHashMap<>();
        for (Course subject : courseRepository.findByParentCourseId(course.getId())) {
            subjectFees.put(subject.getCourseName(), roundMoney(subject.getFees()));
        }
        double subjectsTotal = subjectFees.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalFee = roundMoney(course.getFees() + subjectsTotal + enrollment.getSportsFee()
                + enrollment.getExaminationFee() + enrollment.getOtherCharges() - enrollment.getDiscount());
        double paid = paymentRepository.findByStudentIdAndCourseIdAndStatusIgnoreCase(
                        studentId, course.getId(), "VERIFIED").stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        paid = roundMoney(paid);

        FeeSummaryDTO summary = new FeeSummaryDTO();
        summary.setStudentId(studentId);
        summary.setCourseId(course.getId());
        summary.setCourseName(course.getCourseName());
        summary.setCourseFee(course.getFees());
        summary.setTuitionFee(course.getFees());
        summary.setSubjectFees(subjectFees);
        summary.setSportsFee(enrollment.getSportsFee());
        summary.setExaminationFee(enrollment.getExaminationFee());
        summary.setOtherCharges(enrollment.getOtherCharges());
        summary.setDiscount(enrollment.getDiscount());
        summary.setTotalFee(totalFee);
        summary.setPaidAmount(paid);
        summary.setPendingAmount(roundMoney(Math.max(0, totalFee - paid)));
        summary.setPaymentStatus(paid <= 0 ? "PENDING" : paid >= totalFee ? "PAID" : "PARTIALLY_PAID");
        return summary;
    }

    private PaymentDTO toPaymentDTO(Payment payment) {
        PaymentDTO dto = modelMapper.map(payment, PaymentDTO.class);
        dto.setSignature(null);
        dto.setKeyId(razorpayService.getKeyId());
        return dto;
    }

    private PaymentLedgerDTO toLedgerDTO(Payment payment) {
        PaymentLedgerDTO dto = new PaymentLedgerDTO();
        dto.setId(payment.getId());
        dto.setStudentId(payment.getStudentId());
        dto.setCourseId(payment.getCourseId());
        dto.setAmount(payment.getAmount());
        dto.setOrderId(payment.getOrderId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setTotalFeeAtOrder(payment.getTotalFeeAtOrder());
        dto.setFeeBreakdown(payment.getFeeBreakdown());
        return dto;
    }

    private Map<String, Double> toFeeBreakdown(FeeSummaryDTO feeSummary) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Tuition", feeSummary.getTuitionFee());
        feeSummary.getSubjectFees().forEach((name, amount) -> breakdown.put("Subject: " + name, amount));
        breakdown.put("Sports and extracurricular", feeSummary.getSportsFee());
        breakdown.put("Examination", feeSummary.getExaminationFee());
        breakdown.put("Other charges", feeSummary.getOtherCharges());
        breakdown.put("Discount", -feeSummary.getDiscount());
        breakdown.put("Total fees", feeSummary.getTotalFee());
        breakdown.put("Paid before this transaction", feeSummary.getPaidAmount());
        breakdown.put("Outstanding paid", feeSummary.getPendingAmount());
        return breakdown;
    }

    private long toPaise(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2).longValueExact();
    }

    private double fromPaise(long amount) {
        return BigDecimal.valueOf(amount, 2).doubleValue();
    }

    private double roundMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}