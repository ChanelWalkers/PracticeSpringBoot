package com.example.demo.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpanApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${open.api.title}") String title,
                           @Value("${open.api.version}") String version,
                           @Value("${open.api.description}") String description,
                           @Value("${open.api.serverName}") String serverName,
                           @Value("${open.api.serverURL}") String serverURL){
        return new OpenAPI()
                .info(new Info().title(title)
                        .version(version)
                        .description(description)
                        .license(new License().name("API License").url("http//domain.vn/license"))
                ).servers(List.of(new Server().url(serverURL).description(serverName)));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi(){
        return GroupedOpenApi.builder()
                .group("api-service")
                .packagesToScan("com.example.demo.controller")
                .build();
    }
}
