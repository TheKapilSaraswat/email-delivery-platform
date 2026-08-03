package com.emailplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI emailPlatformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Email Delivery Platform API")
                        .description("Production-ready REST API for campaign, contact, template and analytics management. "
                                + "Authenticate with a JWT bearer token from /api/auth/login.")
                        .version("1.0.0")
                        .contact(new Contact().name("Email Delivery Platform")))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @RestController
    static class SwaggerRedirectController {

        @GetMapping({"/swagger", "/swagger/"})
        public String redirectToSwaggerUi() {
            return "redirect:/swagger-ui/index.html";
        }
    }
}
