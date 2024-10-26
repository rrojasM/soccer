package com.dev_test.soccer.component;

import com.dev_test.soccer.entity.Team;
import com.dev_test.soccer.repository.TeamRespository;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToTeamConverter implements Converter<String, Team> {

    private TeamRespository teamRespository;

    @Override
    public Team convert(String source) {
        return teamRespository.findById(Long.parseLong(source)).orElse(null);
    }
}
