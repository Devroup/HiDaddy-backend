package Devroup.hidaddy.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("HiDaddy API Documentation")
                .version("v1.0.0")
                .description("HiDaddy 백엔드 API 문서입니다.")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 서버 정보 추가 - HTTPS 강제
        Server httpsServer = new Server()
                .url("https://devroup.com")
                .description("Production server (HTTPS)");

        return new OpenAPI()
                .servers(List.of(httpsServer))  // 이 줄이 핵심!
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(info);
    }
}