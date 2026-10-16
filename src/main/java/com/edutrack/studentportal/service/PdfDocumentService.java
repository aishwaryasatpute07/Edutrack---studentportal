package com.edutrack.studentportal.service;

import com.edutrack.studentportal.exception.DocumentGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

@Service
public class PdfDocumentService {

    public byte[] create(String title, List<String> lines) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = newPage(document);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = 760;
            stream.beginText();
            stream.setFont(bold, 18);
            stream.newLineAtOffset(50, y);
            stream.showText(pdfText(title));
            stream.endText();
            y -= 38;

            for (String line : lines) {
                for (String wrapped : wrap(pdfText(line), 92)) {
                    if (y < 55) {
                        stream.close();
                        page = newPage(document);
                        stream = new PDPageContentStream(document, page);
                        y = 760;
                    }
                    stream.beginText();
                    stream.setFont(regular, 11);
                    stream.newLineAtOffset(50, y);
                    stream.showText(wrapped);
                    stream.endText();
                    y -= 17;
                }
            }
            stream.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Could not generate the requested PDF document", exception);
        }
    }

    private PDPage newPage(PDDocument document) {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        return page;
    }

    private String pdfText(String value) {
        String ascii = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "?");
        return ascii.replaceAll("[\\r\\n\\t]+", " ");
    }

    private List<String> wrap(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return List.of(text);
        }
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String remaining = text;
        while (remaining.length() > maxLength) {
            int breakAt = remaining.lastIndexOf(' ', maxLength);
            if (breakAt < 1) {
                breakAt = maxLength;
            }
            lines.add(remaining.substring(0, breakAt));
            remaining = remaining.substring(breakAt).stripLeading();
        }
        lines.add(remaining);
        return lines;
    }
}
