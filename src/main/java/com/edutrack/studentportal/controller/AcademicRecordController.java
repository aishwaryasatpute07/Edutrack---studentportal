package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.AcademicRecordDTO;
import com.edutrack.studentportal.service.AcademicRecordService;
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
@RequestMapping("/academic-records")
public class AcademicRecordController {

    private final AcademicRecordService academicRecordService;
    private final PdfDocumentService pdfDocumentService;

    public AcademicRecordController(AcademicRecordService academicRecordService,
                                    PdfDocumentService pdfDocumentService) {
        this.academicRecordService = academicRecordService;
        this.pdfDocumentService = pdfDocumentService;
    }

    @GetMapping
    public List<AcademicRecordDTO> getAll() {
        return academicRecordService.getAll();
    }

    @GetMapping("/student/{studentId}")
    public List<AcademicRecordDTO> getByStudent(@PathVariable String studentId) {
        return academicRecordService.getByStudent(studentId);
    }

    @GetMapping("/student/{studentId}/marksheet")
    public ResponseEntity<byte[]> downloadMarksheet(@PathVariable String studentId) {
        List<AcademicRecordDTO> records = academicRecordService.getByStudent(studentId);
        StringBuilder csv = new StringBuilder("Student ID,Course ID,Subject ID,Examination,Attempt,Marks,Maximum,Result,Backlog,Updated\r\n");
        records.forEach(record -> csv.append(csv(
                record.getStudentId(), record.getCourseId(), record.getSubjectId(),
                record.getExamination(), String.valueOf(record.getAttemptNumber()), String.valueOf(record.getMarksObtained()),
                String.valueOf(record.getMaximumMarks()), record.isPassed() ? "PASS" : "FAIL",
                record.isBacklog() ? "YES" : "NO",
                record.getUpdatedAt() == null ? "" : record.getUpdatedAt().toString())));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("marksheet-" + safeFilePart(studentId) + ".csv").build().toString())
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/student/{studentId}/marksheet/pdf")
    public ResponseEntity<byte[]> downloadMarksheetPdf(@PathVariable String studentId) {
        List<AcademicRecordDTO> records = academicRecordService.getByStudent(studentId);
        List<String> lines = new java.util.ArrayList<>(List.of("Student ID: " + studentId, ""));
        records.forEach(record -> lines.add(String.format(java.util.Locale.ROOT,
                "%s | %s | Attempt %d | %.2f / %.2f | %s",
                record.getExamination(), record.getSubjectId(), record.getAttemptNumber(),
                record.getMarksObtained(), record.getMaximumMarks(), record.isPassed() ? "PASS" : "BACKLOG")));
        byte[] pdf = pdfDocumentService.create("ACADEMIC MARKSHEET", lines);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("marksheet-" + safeFilePart(studentId) + ".pdf").build().toString())
                .body(pdf);
    }

    @PostMapping
    public AcademicRecordDTO create(@Valid @RequestBody AcademicRecordDTO request) {
        return academicRecordService.save(request);
    }

    @PutMapping("/{id}")
    public AcademicRecordDTO update(@PathVariable String id,
                                    @Valid @RequestBody AcademicRecordDTO request) {
        return academicRecordService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        academicRecordService.delete(id);
    }

    private String csv(String... values) {
        return java.util.Arrays.stream(values)
                .map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "\r\n";
    }

    private String safeFilePart(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
