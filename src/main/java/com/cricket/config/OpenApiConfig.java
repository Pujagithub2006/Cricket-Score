package com.cricket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cricketScoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cricket Score Management System API")
                        .description("REST API specifications for Real-Time Cricket Scoring, Live Match Simulation, Team & Player Management, and Persistent Database Storage.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cricket Live Development Team")
                                .url("http://localhost:8085"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
