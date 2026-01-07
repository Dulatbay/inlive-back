package ai.lab.inlive.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "CAIR Team",
                        email = "ailab@sdu.edu.kz",
                        url = "https://sdu.edu.kz/en/faculty-engineering/"
                ),
                description = "OpenApi documentation for InLive Hotel",
                title = "OpenApi specification - CAIR Team",
                version = "1.0",
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/license/mit/"
                ),
                termsOfService = "https://inlive-hotel.kz/terms"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080/api"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "http://94.131.85.250/api"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Component
public class OpenApiConfig {

    @Bean
    public GlobalOpenApiCustomizer customGlobalHeader() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            Parameter acceptLanguageHeader = new Parameter()
                    .in("header")
                    .name("Accept-Language")
                    .description("Supported locales: kk, ru, en")
                    .schema(new StringSchema()._enum(List.of("kk", "ru", "en"))._default("ru"))
                    .required(false);

            operation.addParametersItem(acceptLanguageHeader);
        }));
    }
}