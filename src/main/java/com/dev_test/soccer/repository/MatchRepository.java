package com.dev_test.soccer.repository;

import com.dev_test.soccer.entity.Match;
import com.dev_test.soccer.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByHomeTeamOrAwayTeam(Team homeTeam, Team awayTeam);
}
