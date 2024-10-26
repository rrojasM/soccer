package com.dev_test.soccer.service;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.MatchRepository;
import com.dev_test.soccer.repository.TeamRespository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
}
