package com.dev_test.soccer;

import com.dev_test.soccer.component.StringToTeamConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private StringToTeamConverter stringToTeamConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToTeamConverter);
    }
}
