package com.dev_test.soccer.controller;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.service.MatchService;
import com.dev_test.soccer.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
//@RequestMapping("/pdf")
public class PdfController {
    private final PdfService pdfService;
    private final MatchService matchService;
    private final MatchRepository matchRepository;

    public PdfController(PdfService pdfService, MatchService matchService, MatchRepository matchRepository) {
        this.pdfService = pdfService;
        this.matchService = matchService;
        this.matchRepository = matchRepository;
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPdf() throws IOException {
        // Obtener la lista de partidos desde el servicio

        List<Match> matches = matchRepository.findAll();

        // Crear un stream para almacenar el PDF
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Generar PDF desde HTML procesado por Thymeleaf
        pdfService.generatePdfFromHtml(stream, matches);

        // Configurar la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=matches.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream.toByteArray());
    }
}
