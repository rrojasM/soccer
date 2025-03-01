package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.AllArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

@Service
@AllArgsConstructor
public class MatchService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public void generateRandomMatches() {
        List<Team> teams = teamRepository.findAll();
        List<Match> matches = new ArrayList<>();
        Random random = new Random();
        LocalDateTime startDate = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).withHour(8);

        Collections.shuffle(teams);
        for (int i = 0; i < teams.size() - 1; i++) {
            for (int j = 0; j < teams.size() - 1; j += 2) {
                if (j + 1 >= teams.size()) break;
                Team homeTeam = teams.get(j);
                Team awayTeam = teams.get(j + 1);
                LocalDateTime matchTime = startDate.plusWeeks(i).plusDays(random.nextInt(3)).withHour(8 + random.nextInt(5));
                Match match = new Match(homeTeam, awayTeam, matchTime);
                matches.add(match);
            }
        }
        matchRepository.saveAll(matches);
    }

    public void generateExcelFile(List<Match> matches, XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Matches");
        String[] headers = {"Match ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador Final"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        int rowNum = 1;
        for (Match match : matches) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(match.getId());
            row.createCell(1).setCellValue(match.getMatchTime().toString());
            row.createCell(2).setCellValue(match.getHomeTeam().getName());
            row.createCell(3).setCellValue(match.getAwayTeam().getName());
            row.createCell(4).setCellValue(match.getHomeTeamScore() + "-" + match.getAwayTeamScore());
        }
    }

    public void generatePdfFile(List<Match> matches, ByteArrayOutputStream stream) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 20, 20, 40, 20);
        PdfWriter.getInstance(document, stream);
        document.open();
        addHeader(document);
        PdfPTable table = createTable(matches);
        document.add(table);
        addFooter(document);
        document.close();
    }

    private void addHeader(Document document) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{20, 80});
        Image logo = Image.getInstance(new URL("https://cpmr-islands.org/wp-content/uploads/sites/4/2019/07/Test-Logo-Small-Black-transparent-1.png"));
        logo.scaleToFit(50, 50);
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
        PdfPCell titleCell = new PdfPCell(new Phrase("Listado de Partidos", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private PdfPTable createTable(List<Match> matches) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{10, 20, 25, 25, 20});
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        String[] headers = {"ID", "Fecha", "Equipo Local", "Equipo Visitante", "Marcador"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(52, 58, 64));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8f);
            table.addCell(cell);
        }
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        for (Match match : matches) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(match.getId()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getMatchTime().format(formatter), cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeam().getName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getAwayTeam().getName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(match.getHomeTeamScore() + " - " + match.getAwayTeamScore(), cellFont)));
        }
        return table;
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("Documento generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }

    public void updateTeamStats(Team team) {
        List<Match> matchesPlayed = matchRepository.findByHomeTeamOrAwayTeam(team, team);

        int wins = 0, draws = 0, losses = 0, goalsFor = 0, goalsAgainst = 0, points = 0;

        for (Match match : matchesPlayed) {
            if (match.getHomeTeam().equals(team)) {
                goalsFor += match.getHomeTeamScore();
                goalsAgainst += match.getAwayTeamScore();
                if (match.getHomeTeamScore() > match.getAwayTeamScore()) {
                    wins++;
                    points += 3;
                } else if (match.getHomeTeamScore() == match.getAwayTeamScore()) {
                    draws++;
                    points += 1;
                } else {
                    losses++;
                }
            } else if (match.getAwayTeam().equals(team)) {
                goalsFor += match.getAwayTeamScore();
                goalsAgainst += match.getHomeTeamScore();
                if (match.getAwayTeamScore() > match.getHomeTeamScore()) {
                    wins++;
                    points += 3;
                } else if (match.getAwayTeamScore() == match.getHomeTeamScore()) {
                    draws++;
                    points += 1;
                } else {
                    losses++;
                }
            }
        }

        team.setMatchesPlayed(wins + draws + losses);
        team.setMatchesWon(wins);
        team.setMatchesDrawn(draws);
        team.setMatchesLost(losses);
        team.setGoalsFor(goalsFor);
        team.setGoalsAgainst(goalsAgainst);
        team.setGoalDifference(goalsFor - goalsAgainst);
        team.setPoints(points);

        teamRepository.save(team);
    }

}
