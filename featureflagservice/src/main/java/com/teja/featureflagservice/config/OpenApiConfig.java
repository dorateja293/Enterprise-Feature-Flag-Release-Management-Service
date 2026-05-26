package com.teja.featureflagservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI enterpriseFeatureFlagOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Feature Flag and Release Management Service")
                        .description("REST APIs for managing feature toggles, environment releases, and audit history.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Platform Engineering"))
                        .license(new License()
                                .name("Internal Enterprise Use")));
    }
}
