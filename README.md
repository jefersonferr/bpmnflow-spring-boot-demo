# BPMNFlow Spring Boot Demo

> A runnable Spring Boot application that demonstrates the [bpmnflow-spring-boot-starter](https://github.com/jefersonferr/bpmnflow-spring-boot-starter) with a real insurance claims process (RDCT - Outros Danos).

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## Table of Contents

- [What this demo shows](#what-this-demo-shows)
- [Prerequisites](#prerequisites)
- [Running the application](#running-the-application)
- [API endpoints](#api-endpoints)
- [Project structure](#project-structure)
- [How it works](#how-it-works)

---

## What this demo shows

This demo illustrates the core value proposition of BPMNFlow: **a Spring Boot application that navigates workflow transitions without any hardcoded business logic**. All routing decisions come from the BPMN model at runtime.

Specifically it demonstrates:

- Zero-config startup — the BPMN model and config are loaded automatically via the starter
- Domain controller (`ClaimsController`) that queries the engine to open cases, resolve next steps, and generate a full process guide
- Swagger UI with full OpenAPI documentation
- The `POST /bpmnflow/model` endpoint for hot-swapping the active BPMN model at runtime

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Access to GitHub Packages (the starter and core are published there)

### GitHub Packages authentication

Add the following to `~/.m2/settings.xml`, replacing `YOUR_TOKEN` with a GitHub Personal Access Token that has `read:packages` scope:

```xml
<settings>
  <servers>
    <server>
      <id>github-starter</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_TOKEN</password>
    </server>
    <server>
      <id>github-core</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_TOKEN</password>
    </server>
  </servers>
</settings>
```

---

## Running the application

```bash
git clone https://github.com/jefersonferr/bpmnflow-spring-boot-demo.git
cd bpmnflow-spring-boot-demo
mvn spring-boot:run
```

The application starts on port `8080`. Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## API endpoints

### Claims (domain demo)

| Method | Path | Description |
|---|---|---|
| `POST` | `/claims/open?caseId=CASE-001` | Opens a new claim case — resolves the first activity from the BPMN model |
| `GET` | `/claims/transition?from=TR-ADR` | Returns all possible next steps from a given activity |
| `GET` | `/claims/guide` | Returns a full process guide — every activity and its outgoing transitions |

### Workflow inspection (from the starter)

| Method | Path | Description |
|---|---|---|
| `GET` | `/bpmnflow/info` | Workflow metadata: name, version, type, health summary |
| `GET` | `/bpmnflow/validate` | Validation result and list of inconsistencies |
| `GET` | `/bpmnflow/activities` | All activities in the workflow |
| `GET` | `/bpmnflow/activities/{abbreviation}` | Single activity by abbreviation (e.g. `TR-ADR`) |
| `GET` | `/bpmnflow/activities/{abbreviation}/next` | Outgoing transitions from a given activity |
| `GET` | `/bpmnflow/stages` | All stages declared in the workflow lanes |
| `GET` | `/bpmnflow/rules` | All workflow rules (transitions) |
| `GET` | `/bpmnflow/rules/by-status?status=NV` | Rules triggered by a given process status |
| `POST` | `/bpmnflow/model` | Upload a new `.bpmn` file to replace the active model at runtime |

---

## Project structure

```
src/main/
├── java/org/bpmnflow/demo/
│   ├── DemoApplication.java       — Spring Boot entry point
│   ├── SwaggerConfig.java         — OpenAPI / Swagger UI configuration
│   └── ClaimsController.java      — Domain endpoints for the claims process
└── resources/
    ├── application.yaml           — Server config and bpmnflow properties
    ├── bpmn-config.yaml           — Validation/extraction rules for the BPMN parser
    └── process.bpmn               — The RDCT-ODN insurance claims BPMN model
```

---

## How it works

The starter auto-configures a `WorkflowEngine` bean by parsing `process.bpmn` against `bpmn-config.yaml` at startup. The `ClaimsController` injects this bean and uses it to:

**Open a case** — queries `rulesTriggeredBy("NV")` to find the START_TO_TASK rule that defines the entry activity for new claims.

**Resolve transitions** — calls `nextSteps(abbreviation)` to get every outgoing path from the current activity, including the conclusion code, resulting process status, and target activity.

**Generate a process guide** — iterates all activities and maps each one to its available exits — useful for populating UI flows or feeding a decision engine without any hardcoded logic.

In a production system these endpoints would be backed by a database of case instances. Here they are stateless to keep the demo self-contained and focused on the BPMNFlow integration.

---

## License

MIT — see [LICENSE](LICENSE).