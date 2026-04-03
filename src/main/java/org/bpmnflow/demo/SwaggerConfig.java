package org.bpmnflow.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@SuppressWarnings("unused")
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BPMNFlow API")
                        .description("""
                                Lightweight BPMN workflow navigation API.
                                
                                Parses a BPMN model at startup and exposes endpoints for:
                                - **Workflow inspection** — metadata, activities, stages, rules
                                - **Navigation** — next steps from any activity, status-driven routing
                                - **Validation** — inconsistencies detected in the BPMN model
                                - **Model upload** — hot-swap the active BPMN model at runtime via POST /bpmnflow/model
                                - **Process** — generic navigation endpoints that work with any active model
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeferson Ferreira")
                                .url("https://github.com/jefersonferr/bpmnflow-core"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .tags(List.of(
                        new Tag().name("Workflow").description("Inspection and navigation of the parsed BPMN model"),
                        new Tag().name("Process").description("Generic process navigation — works with any active BPMN model")
                ));
    }
}