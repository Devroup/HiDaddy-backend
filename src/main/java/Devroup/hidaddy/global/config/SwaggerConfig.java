package Devroup.hidaddy.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
    @Value("${server.port:8080}")
    private String serverPort;

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

        List<Server> servers = getServersForEnvironment();

        return new OpenAPI()
                .servers(servers)
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(info);
    }

    private List<Server> getServersForEnvironment() {
        List<Server> servers = new ArrayList<>();
        
        if ("prod".equals(activeProfile)) {
            // 프로덕션 환경
            servers.add(new Server()
                    .url("https://devroup.com")
                    .description("Production server (HTTPS)"));
        } 
        else {
            // 로컬 환경 (기본값)
            servers.add(new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local development server"));
            // 프로덕션 서버도 추가 (테스트용)
            servers.add(new Server()
                    .url("https://devroup.com")
                    .description("Production server (HTTPS)"));
        }

        return servers;
    }
}