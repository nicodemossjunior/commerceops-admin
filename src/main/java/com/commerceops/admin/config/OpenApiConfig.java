package com.commerceops.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI commerceOpsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CommerceOps Admin API")
                        .description("Administrative API for e-commerce operations.")
                        .version("0.0.1"));
    }
}
