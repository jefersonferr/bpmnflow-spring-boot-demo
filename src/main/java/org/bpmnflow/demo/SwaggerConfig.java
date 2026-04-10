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
                                - **Model upload** — hot-swap the active BPMN model at runtime via POST /process/model
                                - **Process** — generic navigation endpoints that work with any active model

                                ---

                                **Default model: Pizza Delivery Process**

                                The model loaded automatically at startup is a Pizza Delivery process \
                                modelled in BPMN 2.0 with BPMNFlow extension properties. \
                                It covers four swim lanes (Customer, Pizza Shop Clerk, Pizza Chef, Pizza Delivery Guy) \
                                and three exclusive gateways (Order valid?, Pizza ready?, Prepaid?).

                                Download the model file: \
                                [pizza-delivery.bpmn](https://github.com/jefersonferr/bpmnflow-spring-boot-demo/raw/master/src/main/resources/pizza-delivery.bpmn)

                                The model can be opened and edited with \
                                [Camunda Modeler](https://camunda.com/download/modeler/). \
                                BPMNFlow supports both **Camunda 7** and **Camunda 8** models — \
                                the target engine is declared in `bpmn-config.yaml` via the `engine` field \
                                (`camunda7` or `camunda8`).

                                To replace it with your own model at runtime, use **POST /process/model** \
                                with a multipart file upload — no restart required.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeferson Ferreira")
                                .url("https://github.com/jefersonferr/bpmnflow-core"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .tags(List.of(
                        new Tag().name("Process").description("Inspection and navigation of the parsed BPMN model"),
                        new Tag().name("Workflow").description("Generic process navigation — works with any active BPMN model")
                ));
    }
}