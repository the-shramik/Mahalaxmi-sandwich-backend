package com.maven.pos;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@OpenAPIDefinition(
        info = @Info(
                title = "POS-cafe project REST API Documentation",
                description = "POS-cafe application REST API Documentation",
                version="v1",
                contact = @Contact(
                        name = "Codecrafter",
                        email = "info@code-crafter.in",
                        url = "https://code-crafter.in"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "POS-cafe application REST API Documentation",
                url = "https://code-crafter.in"
        )
)
@RestController
@SpringBootApplication
@CrossOrigin("*")
public class PosCafeApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PosCafeApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PosCafeApplication.class, args);
    }

    @GetMapping("/test")
    public String test(){
        return "Application is running...";
    }

}
