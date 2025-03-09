package com.dev_test.soccer.controller;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRepository;
import com.dev_test.soccer.service.MatchService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;

    @GetMapping("/teams")
    public String getTeams(Model model) {
        model.addAttribute("teams", teamRepository.findAll());
        return "teams";
    }

    @GetMapping("/teams/add")
    public String addTeamForm(Model model) {
        model.addAttribute("team", new Team());
        return "add-team";
    }

    @PostMapping("/teams/add")
    public String addTeam(@ModelAttribute Team team) {
        teamRepository.save(team);
        return "redirect:/teams";
    }

    @GetMapping("/matches")
    public String getMatches(Model model) {
        // Obtener todos los partidos de la base de datos una vez
        List<Match> matches = matchRepository.findAll(); // Agrupar partidos por semana del año
        //Map<Integer, List<Match>> matchesByWeek = matches.stream().collect(Collectors.groupingBy(match -> match.getMatchTime().get(WeekFields.ISO.weekOfYear()))); // Añadir atributos al modelo
        Map<Integer, List<Match>> matchesByWeek = matches.stream()
                        .filter(match -> match.getMatchTime() != null)
                                .collect(Collectors.groupingBy((match -> match.getMatchTime().get(WeekFields.ISO.weekOfYear()))));
        model.addAttribute("matchesByWeek", matchesByWeek);
        model.addAttribute("matches", matches); // Retornar la vista
        return "matches";
    }

    @GetMapping("/matches/add")
    public String showAddMatchForm(Model model) {
        model.addAttribute("match", new Match());
        model.addAttribute("teams", teamRepository.findAll());
        return "add-match";
    }

    @PostMapping("/matches/add")
    public String addMatchResult(@ModelAttribute Match match) {
        Team homeTeam = teamRepository.findById(match.getHomeTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid home team Id:" + match.getHomeTeam().getId()));
        Team awayTeam = teamRepository.findById(match.getAwayTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid away team Id:" + match.getAwayTeam().getId()));

        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);

        matchRepository.saveAndFlush(match);
        return "redirect:/matches";
    }

    @GetMapping("/matches/{id}")
    public String getMatchDetails(@PathVariable Long id, Model model) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid match Id:" + id));
        model.addAttribute("match", match);
        model.addAttribute("teams", teamRepository.findAll());
        return "match-details";
    }
    /*
    @PostMapping("/matches/update")
    public String updateMatchResult(@ModelAttribute Match match) {
        Team homeTeam = teamRepository.findById(match.getHomeTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid home team Id:" + match.getHomeTeam().getId()));
        Team awayTeam = teamRepository.findById(match.getAwayTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid away team Id:" + match.getAwayTeam().getId()));

        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);

        matchRepository.save(match);
        return "redirect:/matches";
    }*/
    /*
    @PostMapping("/matches/update")
    public String updateMatchResult(@ModelAttribute Match updatedMatch, Model model) {
        // Buscar el partido existente
        Match existingMatch = matchRepository.findById(updatedMatch.getId())
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado con ID: " + updatedMatch.getId()));

        // Verificar si los equipos son válidos
        Team homeTeam = teamRepository.findById(updatedMatch.getHomeTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid home team ID: " + updatedMatch.getHomeTeam().getId()));
        Team awayTeam = teamRepository.findById(updatedMatch.getAwayTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid away team ID: " + updatedMatch.getAwayTeam().getId()));

        // Actualizar solo los datos necesarios
        existingMatch.setHomeTeam(homeTeam);
        existingMatch.setAwayTeam(awayTeam);
        existingMatch.setMatchTime(updatedMatch.getMatchTime());
        existingMatch.setHomeTeamScore(updatedMatch.getHomeTeamScore());
        existingMatch.setAwayTeamScore(updatedMatch.getAwayTeamScore());

        // Guardar cambios en la BD
        matchRepository.save(existingMatch);

        // Agregar detalles al modelo para mostrar la información actualizada en la vista
        model.addAttribute("match", existingMatch);

        // Redirigir a la vista detallada del partido actualizado
        return "redirect:/matches"; // Esta debería ser una vista donde se muestre el partido con sus detalles
    }*/
    @PostMapping("/matches/update")
    public String updateMatchResult(@ModelAttribute Match updatedMatch, RedirectAttributes redirectAttributes) {
        Match existingMatch = matchRepository.findById(updatedMatch.getId())
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado con ID: " + updatedMatch.getId()));

        existingMatch.setHomeTeamScore(updatedMatch.getHomeTeamScore());
        existingMatch.setAwayTeamScore(updatedMatch.getAwayTeamScore());

        matchRepository.save(existingMatch);

        // Actualizar estadísticas de los equipos
        matchService.updateTeamStats(existingMatch.getHomeTeam());
        matchService.updateTeamStats(existingMatch.getAwayTeam());

        redirectAttributes.addFlashAttribute("successMessage", "Resultado actualizado correctamente");

        return "redirect:/standings";
    }


    @GetMapping("/matches/generate")
    public String generateMatches() {
        matchService.generateRandomMatches();
        return "redirect:/matches";
    }

    @GetMapping("/standings")
    public String getStandings(Model model) {
        List<Team> teams = teamRepository.findAll();

        // Recalcular estadísticas antes de mostrar standings
        teams.forEach(matchService::updateTeamStats);

        // Ordenar por puntos
        teams.sort((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()));

        model.addAttribute("teams", teams);
        return "standings";
    }



    @GetMapping("/home")
    public String home() {
        return "home";
    }
    /*FILES DOWNLOAD*/

    @GetMapping("/excel")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        List<Match> matches = matchRepository.findAll();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Generar el archivo Excel y escribir en el ByteArrayOutputStream
        XSSFWorkbook workbook = new XSSFWorkbook();
        matchService.generateExcelFile(matches, workbook);
        workbook.write(stream);
        workbook.close();

        // Configurar la respuesta HTTP para la descarga
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matches.xlsx");
        response.getOutputStream().write(stream.toByteArray());
        response.flushBuffer();
    }

    /*
    @GetMapping("/pdf")
    public void downloadPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Match> matches = matchRepository.findAll();
        org.apache.commons.io.output.ByteArrayOutputStream stream = new org.apache.commons.io.output.ByteArrayOutputStream();

        // Generar el archivo PDF y escribir en el ByteArrayOutputStream
        matchService.generatePdfFile(matches, stream);

        // Configurar la respuesta HTTP para la descarga
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matches.pdf");
        response.getOutputStream().write(stream.toByteArray());
        response.flushBuffer();
    }*/

}
