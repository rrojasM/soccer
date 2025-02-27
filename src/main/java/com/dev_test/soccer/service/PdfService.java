package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdfFromHtml1(ByteArrayOutputStream stream) throws IOException {
        // **Cargar la plantilla HTML**
        String htmlFilePath = "src/main/resources/templates/pdf-file.html"; // Ruta del HTML
        String htmlContent = new String(Files.readAllBytes(Paths.get(htmlFilePath)));

        // **Convertir HTML a PDF**
        HtmlConverter.convertToPdf(htmlContent, stream);
    }
    public void generatePdfFromHtml(ByteArrayOutputStream stream, List<Match> matches) throws IOException {
        Context context = new Context();
        context.setVariable("matches", matches);

        // Cargar plantilla y procesar con Thymeleaf
        String htmlContent = templateEngine.process("pdf-file", context);

        // Convertir HTML a PDF
        HtmlConverter.convertToPdf(htmlContent, stream);
    }
}
