package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdfFromHtml1(ByteArrayOutputStream stream) throws IOException {
        // **Cargar la plantilla HTML**
        Path htmlFilePath = Path.of("src/main/resources/templates/pdf-file.html"); // Ruta del HTML
        if (Files.exists(htmlFilePath)) {
            String htmlContent = Files.readString(htmlFilePath);
            // **Convertir HTML a PDF**
            HtmlConverter.convertToPdf(htmlContent, stream);
        } else {
            throw new IOException("Archivo HTML no encontrado en: " + htmlFilePath);
        }
    }

    public void generatePdfFromHtml(ByteArrayOutputStream stream, List<Match> matches) throws IOException {
        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("La lista de partidos está vacía o es nula");
        }

        Context context = new Context();
        context.setVariable("matches", matches);

        // Cargar plantilla y procesar con Thymeleaf
        String htmlContent = templateEngine.process("pdf-file", context);

        // Convertir HTML a PDF
        HtmlConverter.convertToPdf(htmlContent, stream);
    }
}