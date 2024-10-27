package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRespository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
    }
}
