package com.pmrs.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI placementManagementApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Placement Record Management System API")
                        .version("1.0")
                        .description("Backend APIs for Placement Record Management System"));
    }
}
