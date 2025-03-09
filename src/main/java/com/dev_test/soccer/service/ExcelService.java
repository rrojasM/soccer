package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Team;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {
    public ResponseEntity<byte[]> generateExcel(List<Team> teams) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Standings");

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);


        Row headerRow = sheet.createRow(0);
        String[] headers = {"Equipo", "Puntos", "Jugados", "Ganados", "Empatados", "Perdidos", "Goles a Favor", "Goles en Contra", "Goles de Diferencia"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (Team team : teams) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(team.getName());
            row.createCell(1).setCellValue(team.getPoints());
            row.createCell(2).setCellValue(team.getMatchesPlayed());
            row.createCell(3).setCellValue(team.getMatchesWon());
            row.createCell(4).setCellValue(team.getMatchesDrawn());
            row.createCell(5).setCellValue(team.getMatchesLost());
            row.createCell(6).setCellValue(team.getGoalsFor());
            row.createCell(7).setCellValue(team.getGoalsAgainst());
            row.createCell(8).setCellValue(team.getGoalDifference());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=standings.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream.toByteArray());
    }

}
