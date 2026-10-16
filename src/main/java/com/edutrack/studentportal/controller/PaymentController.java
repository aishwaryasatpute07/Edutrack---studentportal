package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.FeeSummaryDTO;
import com.edutrack.studentportal.dto.CreatePaymentOrderDTO;
import com.edutrack.studentportal.dto.PaymentDTO;
import com.edutrack.studentportal.dto.PaymentLedgerDTO;
import com.edutrack.studentportal.dto.VerifyPaymentDTO;
import com.edutrack.studentportal.service.PaymentService;
import com.edutrack.studentportal.service.PdfDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PdfDocumentService pdfDocumentService;

    public PaymentController(PaymentService paymentService, PdfDocumentService pdfDocumentService) {
        this.paymentService = paymentService;
        this.pdfDocumentService = pdfDocumentService;
    }

    @GetMapping
    public List<PaymentLedgerDTO> getPaymentLedger() {
        return paymentService.getPaymentLedger();
    }

    @PostMapping("/create-order")
    public PaymentDTO createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderDTO request) {
        return paymentService.createPaymentOrder(request);
    }

    @PostMapping("/verify")
    public PaymentDTO verifyPayment(
            @Valid @RequestBody VerifyPaymentDTO request) {
        return paymentService.verifyPayment(request);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> processWebhook(
            @RequestBody String body,
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.processWebhook(body, signature);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/summary")
    public FeeSummaryDTO getCourseFeeSummary(@PathVariable String courseId) {
        return paymentService.getCourseFeeSummary(courseId);
    }

    @GetMapping("/students/{studentId}/summary")
    public List<FeeSummaryDTO> getStudentFeeSummary(@PathVariable String studentId) {
        return paymentService.getStudentFeeSummary(studentId);
    }

    @GetMapping("/students/{studentId}/history")
    public List<PaymentLedgerDTO> getStudentPaymentHistory(@PathVariable String studentId) {
        return paymentService.getStudentPaymentHistory(studentId);
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String paymentId) {
        PaymentLedgerDTO receipt = paymentService.getReceipt(paymentId);
        StringBuilder csv = new StringBuilder("Receipt,Student ID,Course ID,Amount,Currency,Order ID,Transaction ID,Method,Status,Date\r\n")
                .append(csv("EduTrack", receipt.getStudentId(), receipt.getCourseId(),
                String.valueOf(receipt.getAmount()), "INR", receipt.getOrderId(),
                receipt.getPaymentId(), receipt.getPaymentMethod(), receipt.getStatus(),
                receipt.getPaymentDate() == null ? "" : receipt.getPaymentDate().toString()));
        csv.append("Fee item,Amount\r\n");
        receipt.getFeeBreakdown().forEach((item, amount) ->
                csv.append(csv(item, String.valueOf(amount))));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("receipt-" + safeFilePart(paymentId) + ".csv").build().toString())
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{paymentId}/receipt/pdf")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable String paymentId) {
        PaymentLedgerDTO receipt = paymentService.getReceipt(paymentId);
        List<String> lines = new java.util.ArrayList<>(List.of(
                "Student ID: " + safeText(receipt.getStudentId()),
                "Course ID: " + safeText(receipt.getCourseId()),
                "Amount received: INR " + String.format(java.util.Locale.ROOT, "%.2f", receipt.getAmount()),
                "Order ID: " + safeText(receipt.getOrderId()),
                "Transaction ID: " + safeText(receipt.getPaymentId()),
                "Payment method: " + safeText(receipt.getPaymentMethod()),
                "Status: " + safeText(receipt.getStatus()),
                "Payment date: " + safeText(receipt.getPaymentDate()),
                "",
                "Fee breakdown:"));
        receipt.getFeeBreakdown().forEach((item, amount) ->
                lines.add(item + ": INR " + String.format(java.util.Locale.ROOT, "%.2f", amount)));
        byte[] pdf = pdfDocumentService.create("EDUTRACK PAYMENT RECEIPT", lines);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("receipt-" + safeFilePart(paymentId) + ".pdf").build().toString())
                .body(pdf);
    }

    private String csv(String... values) {
        return java.util.Arrays.stream(values)
                .map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "\r\n";
    }

    private String safeFilePart(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String safeText(Object value) {
        return value == null ? "" : value.toString();
    }
}