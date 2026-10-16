package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.HallTicketDTO;
import com.edutrack.studentportal.service.HallTicketService;
import com.edutrack.studentportal.service.PdfDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hall-tickets")
public class HallTicketController {

    private final HallTicketService hallTicketService;
    private final PdfDocumentService pdfDocumentService;

    public HallTicketController(HallTicketService hallTicketService,
                                PdfDocumentService pdfDocumentService) {
        this.hallTicketService = hallTicketService;
        this.pdfDocumentService = pdfDocumentService;
    }

    @GetMapping
    public List<HallTicketDTO> getAll() {
        return hallTicketService.getAll();
    }

    @GetMapping("/student/{studentId}")
    public List<HallTicketDTO> getByStudent(@PathVariable String studentId) {
        return hallTicketService.getByStudent(studentId);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        HallTicketDTO ticket = hallTicketService.getForDownload(id);
        byte[] pdf = pdfDocumentService.create("EXAMINATION HALL TICKET", List.of(
                "Student: " + ticket.getStudentName(),
                "Student ID: " + ticket.getStudentId(),
                "Course: " + ticket.getCourseName(),
                "Examination: " + ticket.getExamination(),
                "Exam date: " + ticket.getExamDate(),
                "Start time: " + ticket.getStartTime(),
                "Venue: " + ticket.getVenue(),
                "Seat number: " + ticket.getSeatNumber(),
                "Instructions: " + (ticket.getInstructions() == null || ticket.getInstructions().isBlank()
                        ? "Follow the examination centre instructions." : ticket.getInstructions()),
                "Issued: " + ticket.getIssuedAt()));
        return pdfResponse(pdf, "hall-ticket-" + safeFilePart(ticket.getId()) + ".pdf");
    }

    @PostMapping
    public HallTicketDTO create(@Valid @RequestBody HallTicketDTO request) {
        return hallTicketService.create(request);
    }

    @PutMapping("/{id}")
    public HallTicketDTO update(@PathVariable String id, @Valid @RequestBody HallTicketDTO request) {
        return hallTicketService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        hallTicketService.delete(id);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    private String safeFilePart(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
