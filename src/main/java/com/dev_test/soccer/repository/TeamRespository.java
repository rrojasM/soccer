package com.dev_test.soccer.repository;

import com.dev_test.soccer.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRespository extends JpaRepository<Team, Long> {
}
