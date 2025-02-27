package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRespository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.*;


@Service
@AllArgsConstructor
public class MatchService {
    private TeamRespository teamRespository;
    private MatchRepository matchRepository;


    public void generateRandomMatches(){

        List<Team> teams = teamRespository.findAll();
        List<Match> matches = new ArrayList<>();
        Random random = new Random();
        LocalDateTime startDate = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).withHour(8);

        for (int i = 0; i < teams.size() - 1; i++) {
            Collections.shuffle(teams);
            for (int j = 0; j < teams.size(); j += 2) {
                Team homeTeam = teams.get(j);
                Team awayTeam = teams.get(j + 1);
                LocalDateTime matchTime = startDate.plusWeeks(i).plusDays(random.nextInt(3)).withHour(8 + random.nextInt(5));
                Match match = new Match();
                match.setHomeTeam(homeTeam);
                match.setAwayTeam(awayTeam);
                System.out.println("MatchTime: " + matchTime);
                match.setMatchTime(matchTime);
                matches.add(match);
            }
        }
        matchRepository.saveAll(matches);
    }

    public void generateExcelFile(List<Match> matches, XSSFWorkbook workbook) throws IOException {
        XSSFSheet sheet = workbook.createSheet("Matches");

        // Crear el encabezado
        String[] headers = {"Match ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador Final"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Añadir los datos de los partidos
        int rowNum = 1;
        for (Match match : matches) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(match.getHomeTeam().getId());
            row.createCell(1).setCellValue(match.getMatchTime().toString());
            row.createCell(2).setCellValue(match.getHomeTeam().getName());
            row.createCell(3).setCellValue(match.getAwayTeam().getName());
            row.createCell(4).setCellValue(match.getHomeTeamScore() + "-" + match.getAwayTeamScore());
            System.out.println("MATCH " + match.getHomeTeam().getId());
        }


    }
    /*
    public void generatePdfFile(List<Match> matches, ByteArrayOutputStream stream) throws DocumentException, FileNotFoundException {
        Document document = new Document();
        PdfWriter.getInstance(document, stream);

        document.open();

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        String[] headers = {"ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new com.itextpdf.text.Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Match match : matches) {
            table.addCell(String.valueOf(match.getId()));
            table.addCell(match.getMatchTime().toString());
            table.addCell(match.getHomeTeam().getName());
            table.addCell(match.getAwayTeam().getName());
            table.addCell(match.getHomeTeamScore() + "-" + match.getAwayTeamScore()); // Assumes match has a getScore method
        }

        document.add(table);
        document.close();
    }*/
    public void generatePdfFile3(List<Match> matches, ByteArrayOutputStream stream) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 20, 20, 40, 20);
        PdfWriter writer = PdfWriter.getInstance(document, stream);
        document.open();

        // **1. Agregar encabezado con título y logo**
        addHeader(document);

        // **2. Crear tabla con diseño mejorado**
        PdfPTable table = createTable(matches);
        document.add(table);

        // **3. Agregar pie de página**
        addFooter(document);

        document.close();
    }

    // **Encabezado con logo y título**
    private void addHeader(Document document) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{20, 80});

        // **Logo**
        String logoUrl = "https://cpmr-islands.org/wp-content/uploads/sites/4/2019/07/Test-Logo-Small-Black-transparent-1.png";
        Image logo = Image.getInstance(new URL(logoUrl)); // Ruta del logo
        logo.scaleToFit(50, 50);
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        // **Título**
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,16,com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
        /*Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);*/
        PdfPCell titleCell = new PdfPCell(new Phrase("Listado de Partidos", (com.itextpdf.text.Font) titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("\n")); // Espaciado
    }

    // **Crea la tabla con estilos**
    private PdfPTable createTable(List<Match> matches) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{10, 20, 25, 25, 20});

        // **Encabezados con fondo azul**
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        String[] headers = {"ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(0, 102, 204));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8f);
            table.addCell(cell);
        }

        // **Filas de datos**
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        for (Match match : matches) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(match.getId()), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getMatchTime().format(formatter), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeam().getName(), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getAwayTeam().getName(), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeamScore() + " - " + match.getAwayTeamScore(), (com.itextpdf.text.Font) cellFont)));
        }

        return table;
    }

    // **Pie de página con fecha de generación**
    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("Documento generado el " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }

    /*TEST*/
    public void generatePdfFile(List<Match> matches, ByteArrayOutputStream stream) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 20, 20, 40, 20);
        PdfWriter.getInstance(document, stream);
        document.open();

        // **Encabezado con logo y título**
        addHeader(document);

        // **Tabla con partidos**
        PdfPTable table = createTable(matches);
        document.add(table);

        // **Pie de página con fecha**
        addFooter(document);

        document.close();
    }

    private void addHeader2(Document document) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{20, 80});

        // **Cargar logo desde URL**
        String logoUrl = "https://example.com/images/logo.png"; // Reemplaza con tu URL real
        Image logo = Image.getInstance(new URL(logoUrl));
        logo.scaleToFit(50, 50);
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        // **Título**
        com.itextpdf.text.Font titleFont = new  com.itextpdf.text.Font( com.itextpdf.text.Font.FontFamily.HELVETICA, 16,  com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
        PdfPCell titleCell = new PdfPCell(new Phrase("Listado de Partidos", (com.itextpdf.text.Font) titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private PdfPTable createTable2(List<Match> matches) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{10, 20, 25, 25, 20});

        // **Encabezados**
        com.itextpdf.text.Font headerFont = new  com.itextpdf.text.Font( com.itextpdf.text.Font.FontFamily.HELVETICA, 12,  com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        String[] headers = {"ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, (com.itextpdf.text.Font) headerFont));
            cell.setBackgroundColor(new BaseColor(0, 102, 204));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8f);
            table.addCell(cell);
        }

        // **Datos**
        com.itextpdf.text.Font cellFont = new  com.itextpdf.text.Font( com.itextpdf.text.Font.FontFamily.HELVETICA, 11,  com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        for (Match match : matches) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(match.getId()), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getMatchTime().format(formatter), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeam().getName(), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getAwayTeam().getName(), (com.itextpdf.text.Font) cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeamScore() + " - " + match.getAwayTeamScore(), (com.itextpdf.text.Font) cellFont)));
        }

        return table;
    }

    private void addFooter2(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("Documento generado el " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                new  com.itextpdf.text.Font( com.itextpdf.text.Font.FontFamily.HELVETICA, 10,  com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }
}
