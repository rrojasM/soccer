package com.dev_test.soccer.controller;

import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.TeamRepository;
import com.dev_test.soccer.service.ExcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/excelStandings")
public class ExcelController {
    private final ExcelService excelService;
    private final TeamRepository teamRepository;

    public ExcelController(ExcelService excelService, TeamRepository teamRepository) {
        this.excelService = excelService;
        this.teamRepository = teamRepository;
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        List<Team> teams = teamRepository.findAll();
        return  excelService.generateExcel(teams);
    }
}
