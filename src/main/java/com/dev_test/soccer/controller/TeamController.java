package com.dev_test.soccer.controller;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRespository;
import com.dev_test.soccer.service.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class TeamController {

    private TeamRespository teamRepository;
    private MatchRepository matchRepository;
    private MatchService matchService;

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
        List<Match> matches = matchRepository.findAll();
        Map<Integer, List<Match>> matchesByWeek = matches.stream()
                .collect(Collectors.groupingBy(match -> match.getMatchTime().get(WeekFields.ISO.weekOfYear())));

        model.addAttribute("matchesByWeek", matchesByWeek);
        return "matches";
        //model.addAttribute("matches", matchRepository.findAll());
        //return "matches";
    }

    @GetMapping("/matches/add")
    public String showAddMatchForm(Model model) {
        model.addAttribute("match", new Match());
        model.addAttribute("teams", teamRepository.findAll());
        return "add-match";
    }

    @PostMapping("/matches/add")
    public String addMatchResult(@ModelAttribute Match match) {
        matchRepository.save(match);
        return "redirect:/matches";
    }

    @GetMapping("/matches/{id}")
    public String getMatchDetails(@PathVariable Long id, Model model) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid match Id:" + id));
        model.addAttribute("match", match);
        model.addAttribute("teams", teamRepository.findAll());
        return "match-details";
    }

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
    }

    @GetMapping("/matches/generate")
    public String generateMatches() {
        matchService.generateRandomMatches();
        return "redirect:/matches";
    }

    @GetMapping("/standings")
    public String getStandings(Model model) {
        List<Team> teams = teamRepository.findAll();
        teams.sort((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()));
        model.addAttribute("teams", teams);
        return "standings";
    }
}
